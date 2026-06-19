package kurou.kodriver.data.repository

import kurou.kodriver.data.datasource.MemoryReader
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal class FakeMemoryReader(
    initialOpen: Boolean = false,
    openResults: List<Boolean> = listOf(true),
    private val returnNullBuffer: Boolean = false,
) : MemoryReader {

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
