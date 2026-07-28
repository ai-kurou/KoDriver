package kurou.kodriver.core.windowssharedmemory.datasource

import java.nio.ByteBuffer

interface SharedMemoryReader {
    fun open(): Boolean
    fun readBuffer(): ByteBuffer?
    fun isOpen(): Boolean
    fun close()
}
