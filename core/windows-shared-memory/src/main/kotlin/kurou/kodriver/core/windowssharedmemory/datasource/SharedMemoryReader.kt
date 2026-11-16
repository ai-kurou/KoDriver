package kurou.kodriver.core.windowssharedmemory.datasource

import java.nio.ByteBuffer

/**
 * Windows 共有メモリを読み取る reader。
 */
public interface SharedMemoryReader {
    public fun open(): Boolean

    public fun readBuffer(): ByteBuffer?

    public fun isOpen(): Boolean

    public fun close()
}
