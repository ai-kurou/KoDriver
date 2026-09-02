package kurou.kodriver.core.windowssharedmemory.datasource

import java.nio.ByteBuffer
import java.nio.ByteOrder

class FakeWindowsSharedMemoryReader(
    initialOpen: Boolean = false,
    openResults: List<Boolean> = listOf(true),
    private val bufferSize: Int,
    private val returnNullBuffer: Boolean = false,
    private val configureBuffer: (ByteBuffer) -> Unit = {},
) : SharedMemoryReader {
    private var opened = initialOpen
    private val remainingOpenResults = ArrayDeque(openResults)
    var closeCalled = false
    var openCallCount = 0
        private set

    override fun open(): Boolean {
        openCallCount++
        opened = remainingOpenResults.removeFirstOrNull() ?: false
        return opened
    }

    override fun readBuffer(): ByteBuffer? =
        if (opened && !returnNullBuffer) {
            ByteBuffer.allocate(bufferSize).order(ByteOrder.LITTLE_ENDIAN).also(configureBuffer)
        } else {
            null
        }

    override fun isOpen(): Boolean = opened

    override fun close() {
        closeCalled = true
        opened = false
    }
}
