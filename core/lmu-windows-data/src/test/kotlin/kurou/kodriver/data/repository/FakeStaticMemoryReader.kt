package kurou.kodriver.data.repository

import kurou.kodriver.data.datasource.MemoryReader
import java.nio.ByteBuffer

internal class FakeStaticMemoryReader(
    private val buffer: ByteBuffer,
    initialOpen: Boolean = true,
    private val openResult: Boolean = true,
) : MemoryReader {

    private var opened = initialOpen
    var closeCalled = false

    override fun open(): Boolean {
        opened = openResult
        return openResult
    }

    override fun readBuffer(): ByteBuffer? = if (opened) buffer.duplicate() else null

    override fun isOpen(): Boolean = opened

    override fun close() {
        closeCalled = true
        opened = false
    }
}
