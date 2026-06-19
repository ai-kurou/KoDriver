package kurou.kodriver.core.lmuwindowsdata.datasource

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SharedLmuWindowsMemorySourceTest {

    private fun makeSource(
        reader: FakeMemoryReader,
        pollingIntervalMs: Long = 1L,
        reconnectIntervalMs: Long = 1L,
        currentTimeMs: () -> Long = System::currentTimeMillis,
    ) = SharedLmuWindowsMemorySource(
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
    fun `open 成功後に bufferFlow がバッファを emit する`() = runBlocking<Unit> {
        val reader = FakeMemoryReader(initialOpen = true)
        val source = makeSource(reader)

        source.bufferFlow.first()
    }

    @Test
    fun `open 失敗中は bufferFlow が emit しない`() = runBlocking {
        val reader = FakeMemoryReader(initialOpen = false, openResult = false)
        val source = makeSource(reader)
        var emitCount = 0

        val job = launch { source.bufferFlow.collect { emitCount++ } }
        delay(50)
        job.cancelAndJoin()

        assertTrue(emitCount == 0)
    }

    @Test
    fun `bufferFlow がキャンセルされると reader の close が呼ばれる`() = runBlocking {
        val reader = FakeMemoryReader(initialOpen = true)
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
    fun `open に成功しバッファを読み取れるとき isConnected は true を返す`() = runBlocking {
        val source = makeSource(reader = FakeMemoryReader(openResult = true))

        assertTrue(source.isConnected())
    }

    @Test
    fun `open に失敗するとき isConnected は false を返す`() = runBlocking {
        val source = makeSource(reader = FakeMemoryReader(openResult = false))

        assertFalse(source.isConnected())
    }

    @Test
    fun `isConnected は reader を close してから open する`() = runBlocking {
        val reader = FakeMemoryReader(openResult = true)
        val source = makeSource(reader = reader)

        source.isConnected()

        assertTrue(reader.closeCalled)
    }

    @Test
    fun `バッファを読み取れないとき isConnected は false を返す`() = runBlocking {
        val source = makeSource(reader = FakeMemoryReader(openResult = true, returnNullBuffer = true))

        assertFalse(source.isConnected())
    }

    @Test
    fun `mCurrentET が閾値以内に変化し続けるとき isConnected は true を返す`() = runBlocking {
        var fakeTime = 0L
        val reader = FakeMemoryReader(openResult = true, currentEt = 1.0)
        val source = makeSource(reader = reader, currentTimeMs = { fakeTime })

        fakeTime = 0L
        reader.currentEt = 1.0
        assertTrue(source.isConnected())
        fakeTime = 1_000L
        reader.currentEt = 2.0
        assertTrue(source.isConnected())
        fakeTime = 2_000L
        reader.currentEt = 3.0
        assertTrue(source.isConnected())
    }

    @Test
    fun `mCurrentET が閾値以上変化しないとき isConnected は false を返す`() = runBlocking {
        var fakeTime = 0L
        val reader = FakeMemoryReader(openResult = true, currentEt = 500.0)
        val source = makeSource(reader = reader, currentTimeMs = { fakeTime })

        fakeTime = 0L
        source.isConnected() // 初回: タイムスタンプ = 0
        fakeTime = 3_000L
        assertFalse(source.isConnected())
    }

    // -------------------------------------------------------------------------
    // disconnect
    // -------------------------------------------------------------------------

    @Test
    fun `disconnect は reader の close を呼ぶ`() = runBlocking {
        val reader = FakeMemoryReader(initialOpen = true)
        val source = makeSource(reader)

        source.disconnect()

        assertTrue(reader.closeCalled)
    }
}

// -----------------------------------------------------------------------------
// ヘルパー
// -----------------------------------------------------------------------------

private class FakeMemoryReader(
    initialOpen: Boolean = false,
    private val openResult: Boolean = true,
    private val returnNullBuffer: Boolean = false,
    var currentEt: Double = 0.0,
) : MemoryReader {

    private var opened = initialOpen
    var closeCalled = false

    override fun open(): Boolean {
        opened = openResult
        return openResult
    }

    override fun readBuffer(): ByteBuffer? =
        if (opened && !returnNullBuffer) {
            ByteBuffer.allocate(200_000).order(ByteOrder.LITTLE_ENDIAN).also { buf ->
                buf.putDouble(1700, currentEt)
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
