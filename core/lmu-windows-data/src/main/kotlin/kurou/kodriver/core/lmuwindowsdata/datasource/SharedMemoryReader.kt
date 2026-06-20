package kurou.kodriver.core.lmuwindowsdata.datasource

import java.nio.ByteBuffer

internal interface SharedMemoryReader {
    fun open(): Boolean
    fun readBuffer(): ByteBuffer?
    fun isOpen(): Boolean
    fun close()
}
