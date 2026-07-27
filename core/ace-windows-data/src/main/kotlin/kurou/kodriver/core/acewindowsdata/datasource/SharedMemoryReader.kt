package kurou.kodriver.core.acewindowsdata.datasource

import java.nio.ByteBuffer

internal interface SharedMemoryReader {
    fun open(): Boolean
    fun readBuffer(): ByteBuffer?
    fun isOpen(): Boolean
    fun close()
}
