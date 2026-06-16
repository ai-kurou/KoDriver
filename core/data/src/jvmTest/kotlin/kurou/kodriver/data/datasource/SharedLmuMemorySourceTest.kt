package kurou.kodriver.data.datasource

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

class SharedLmuMemorySourceTest {

    private fun makeSource(
        reader: FakeMemoryReader,
        probeReader: FakeMemoryReader = reader,
        pollingIntervalMs: Long = 1L,
        reconnectIntervalMs: Long = 1L,
    ) = SharedLmuMemorySource(
        pollingIntervalMs = pollingIntervalMs,
        reconnectIntervalMs = reconnectIntervalMs,
        reader = reader,
        probeReaderFactory = { probeReader },
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
        val probe = FakeMemoryReader(openResult = true)
        val source = makeSource(reader = FakeMemoryReader(), probeReader = probe)

        assertTrue(source.isConnected())
    }

    @Test
    fun `open に失敗するとき isConnected は false を返す`() = runBlocking {
        val probe = FakeMemoryReader(openResult = false)
        val source = makeSource(reader = FakeMemoryReader(), probeReader = probe)

        assertFalse(source.isConnected())
    }

    @Test
    fun `isConnected はプローブリーダーを open 後に close する`() = runBlocking {
        val probe = FakeMemoryReader(openResult = true)
        val source = makeSource(reader = FakeMemoryReader(), probeReader = probe)

        source.isConnected()

        assertTrue(probe.closeCalled)
    }

    @Test
    fun `バッファを読み取れないとき isConnected は false を返す`() = runBlocking {
        val probe = FakeMemoryReader(openResult = true, returnNullBuffer = true)
        val source = makeSource(reader = FakeMemoryReader(), probeReader = probe)

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
) : MemoryReader {

    private var opened = initialOpen
    var closeCalled = false

    override fun open(): Boolean {
        opened = openResult
        return openResult
    }

    override fun readBuffer(): ByteBuffer? =
        if (opened && !returnNullBuffer) {
            ByteBuffer.allocate(135_000).order(ByteOrder.LITTLE_ENDIAN)
        } else {
            null
        }

    override fun isOpen(): Boolean = opened

    override fun close() {
        closeCalled = true
        opened = false
    }
}
