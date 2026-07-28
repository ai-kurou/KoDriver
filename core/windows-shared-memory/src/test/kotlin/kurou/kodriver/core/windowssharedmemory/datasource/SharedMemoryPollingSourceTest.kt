package kurou.kodriver.core.windowssharedmemory.datasource

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
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SharedMemoryPollingSourceTest {

    private fun makeSource(
        reader: FakeSharedMemoryReader,
        pollingIntervalMs: Long = 1L,
        reconnectIntervalMs: Long = 1L,
    ) = SharedMemoryPollingSource(
        reader = reader,
        pollingIntervalMs = pollingIntervalMs,
        reconnectIntervalMs = reconnectIntervalMs,
        scope = CoroutineScope(SupervisorJob()),
    )

    @Test
    fun `open 成功後に bufferFlow がバッファを emit する`() = runBlocking<Unit> {
        val reader = FakeSharedMemoryReader(initialOpen = true)
        val source = makeSource(reader)

        source.bufferFlow.first()
    }

    @Test
    fun `open 失敗中は bufferFlow が emit しない`() = runBlocking {
        val reader = FakeSharedMemoryReader(initialOpen = false, openResult = false)
        val source = makeSource(reader)
        var emitCount = 0

        val job = launch { source.bufferFlow.collect { emitCount++ } }
        delay(50)
        job.cancelAndJoin()

        assertTrue(emitCount == 0)
    }

    @Test
    fun `bufferFlow がキャンセルされると reader の close が呼ばれる`() = runBlocking {
        val reader = FakeSharedMemoryReader(initialOpen = true)
        val source = makeSource(reader)

        val job = launch { source.bufferFlow.collect { } }
        delay(50)
        job.cancelAndJoin()
        // WhileSubscribed が IO スレッドへ cancellation を伝播するまで待機
        delay(100)

        assertTrue(reader.closeCalled)
    }

    @Test
    fun `withReaderLock は reader を渡して block を実行する`() = runBlocking {
        val reader = FakeSharedMemoryReader(initialOpen = true)
        val source = makeSource(reader)

        val result = source.withReaderLock { r -> r.isOpen() }

        assertEquals(true, result)
    }
}

private class FakeSharedMemoryReader(
    initialOpen: Boolean = false,
    private val openResult: Boolean = true,
    private val returnNullBuffer: Boolean = false,
) : SharedMemoryReader {

    private var opened = initialOpen
    var closeCalled = false

    override fun open(): Boolean {
        opened = openResult
        return openResult
    }

    override fun readBuffer(): ByteBuffer? =
        if (opened && !returnNullBuffer) {
            ByteBuffer.allocate(8_192).order(ByteOrder.LITTLE_ENDIAN)
        } else {
            null
        }

    override fun isOpen(): Boolean = opened

    override fun close() {
        closeCalled = true
        opened = false
    }
}
