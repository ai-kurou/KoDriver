package kurou.kodriver.core.windowssharedmemory.datasource

import java.nio.ByteBuffer

/**
 * Windows 共有メモリを読み取る reader。
 */
interface SharedMemoryReader {
    fun open(): Boolean

    fun readBuffer(): ByteBuffer?

    fun isOpen(): Boolean

    fun close()
}
