package kurou.kodriver.core.acewindowsdata.datasource

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kurou.kodriver.core.windowssharedmemory.datasource.SharedMemoryReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AceWindowsGraphicsSharedMemorySourceTest {
    private fun makeSource(
        reader: FakeSharedMemoryReader,
        pollingIntervalMs: Long = 1L,
        reconnectIntervalMs: Long = 1L,
        currentTimeMs: () -> Long = System::currentTimeMillis,
    ) = AceWindowsGraphicsSharedMemorySource(
        pollingIntervalMs = pollingIntervalMs,
        reconnectIntervalMs = reconnectIntervalMs,
        reader = reader,
        currentTimeMs = currentTimeMs,
        scope = CoroutineScope(SupervisorJob()),
    )

    // -------------------------------------------------------------------------
    // bufferFlow
    // -------------------------------------------------------------------------

    @Test
    fun `デフォルト reader は ACE Graphics 共有メモリ名とサイズで構成される`() {
        var segmentName: String? = null
        var sizeBytes: Int? = null
        val reader = FakeSharedMemoryReader()
        val source =
            AceWindowsGraphicsSharedMemorySource(
                readerFactory = { name, size ->
                    segmentName = name
                    sizeBytes = size
                    reader
                },
                scope = CoroutineScope(SupervisorJob()),
            )

        assertTrue(source.reader === reader)
        assertEquals("Local\\acevo_pmf_graphics", segmentName)
        assertEquals(8_192, sizeBytes)
    }

    @Test
    fun `open 成功後に bufferFlow がバッファを emit する`() =
        runBlocking<Unit> {
            val reader = FakeSharedMemoryReader(initialOpen = true)
            val source = makeSource(reader)

            source.bufferFlow.first()
        }

    @Test
    fun `同じ packetId の古いバッファは再配信せず新しい packetId のバッファを配信する`() =
        runBlocking {
            val reader = FakeSharedMemoryReader(initialOpen = true, packetId = 1)
            val source = makeSource(reader)
            var emitCount = 0
            val secondEmission = CompletableDeferred<Unit>()

            val job =
                launch {
                    source.bufferFlow.collect {
                        emitCount++
                        if (emitCount == 2) secondEmission.complete(Unit)
                    }
                }

            withTimeout(1_000) {
                while (emitCount == 0) delay(1)
            }
            delay(10)
            assertEquals(1, emitCount)

            reader.packetId = 2
            withTimeout(1_000) { secondEmission.await() }
            job.cancelAndJoin()

            assertEquals(2, emitCount)
        }

    @Test
    fun `open 失敗中は bufferFlow が emit しない`() =
        runBlocking {
            val reader = FakeSharedMemoryReader(initialOpen = false, openResult = false)
            val source = makeSource(reader)
            var emitCount = 0

            val job = launch { source.bufferFlow.collect { emitCount++ } }
            delay(50)
            job.cancelAndJoin()

            assertTrue(emitCount == 0)
        }

    @Test
    fun `bufferFlow がキャンセルされると reader の close が呼ばれる`() =
        runBlocking {
            val reader = FakeSharedMemoryReader(initialOpen = true)
            val source = makeSource(reader)

            val job = launch { source.bufferFlow.collect { } }
            delay(50)
            job.cancelAndJoin()
            // WhileSubscribed が IO スレッドへ cancellation を伝播するまで待機
            delay(100)

            assertTrue(reader.closeCalled)
        }

    // -------------------------------------------------------------------------
    // isConnected
    // -------------------------------------------------------------------------

    @Test
    fun `open に成功しバッファを読み取れるとき isConnected は true を返す`() =
        runBlocking {
            val source = makeSource(reader = FakeSharedMemoryReader(openResult = true))

            assertTrue(source.isConnected())
        }

    @Test
    fun `open に失敗するとき isConnected は false を返す`() =
        runBlocking {
            val source = makeSource(reader = FakeSharedMemoryReader(openResult = false))

            assertFalse(source.isConnected())
        }

    @Test
    fun `isConnected は reader を close してから open する`() =
        runBlocking {
            val reader = FakeSharedMemoryReader(openResult = true)
            val source = makeSource(reader = reader)

            source.isConnected()

            assertTrue(reader.closeCalled)
        }

    @Test
    fun `バッファを読み取れないとき isConnected は false を返す`() =
        runBlocking {
            val source = makeSource(reader = FakeSharedMemoryReader(openResult = true, returnNullBuffer = true))

            assertFalse(source.isConnected())
        }

    @Test
    fun `packetId が閾値以内に変化し続けるとき isConnected は true を返す`() =
        runBlocking {
            var fakeTime = 0L
            val reader = FakeSharedMemoryReader(openResult = true, packetId = 1)
            val source = makeSource(reader = reader, currentTimeMs = { fakeTime })

            fakeTime = 0L
            reader.packetId = 1
            assertTrue(source.isConnected())
            fakeTime = 1_000L
            reader.packetId = 2
            assertTrue(source.isConnected())
            fakeTime = 2_000L
            reader.packetId = 3
            assertTrue(source.isConnected())
        }

    @Test
    fun `packetId が閾値以上変化しないとき isConnected は false を返す`() =
        runBlocking {
            var fakeTime = 0L
            val reader = FakeSharedMemoryReader(openResult = true, packetId = 500)
            val source = makeSource(reader = reader, currentTimeMs = { fakeTime })

            fakeTime = 0L
            source.isConnected() // 初回: タイムスタンプ = 0
            fakeTime = 3_000L
            assertFalse(source.isConnected())
        }
}

// -----------------------------------------------------------------------------
// ヘルパー
// -----------------------------------------------------------------------------

private class FakeSharedMemoryReader(
    initialOpen: Boolean = false,
    private val openResult: Boolean = true,
    private val returnNullBuffer: Boolean = false,
    var packetId: Int = 0,
) : SharedMemoryReader {
    private var opened = initialOpen
    var closeCalled = false

    override fun open(): Boolean {
        opened = openResult
        return openResult
    }

    override fun readBuffer(): ByteBuffer? =
        if (opened && !returnNullBuffer) {
            ByteBuffer.allocate(8_192).order(ByteOrder.LITTLE_ENDIAN).also { buf ->
                buf.putInt(0, packetId)
            }
        } else {
            null
        }

    override fun isOpen(): Boolean = opened

    override fun close() {
        closeCalled = true
        opened = false
    }
}
