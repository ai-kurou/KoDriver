package kurou.kodriver.core.lmuwindowsdata.repository

import kurou.kodriver.core.lmuwindowsdata.datasource.SharedMemoryReader
import java.nio.ByteBuffer

internal class FakeStaticSharedMemoryReader(
    private val buffer: ByteBuffer,
    initialOpen: Boolean = true,
    private val openResult: Boolean = true,
) : SharedMemoryReader {

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
