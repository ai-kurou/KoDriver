package kurou.kodriver.data.repository

import kurou.kodriver.data.datasource.MemoryReader
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal class FakeMemoryReader(
    initialOpen: Boolean = false,
    private val openResult: Boolean = true,
) : MemoryReader {

    private var opened = initialOpen
    var closeCalled = false

    override fun open(): Boolean {
        opened = openResult
        return openResult
    }

    override fun readBuffer(): ByteBuffer? =
        if (opened) ByteBuffer.allocate(135_000).order(ByteOrder.LITTLE_ENDIAN) else null

    override fun isOpen(): Boolean = opened

    override fun close() {
        closeCalled = true
        opened = false
    }
}
