package kurou.kodriver.data.datasource

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinNT.HANDLE
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal class SharedMemoryReader(
    private val segmentName: String,
    private val sizeBytes: Int,
    private val isWindows: Boolean = System.getProperty("os.name").contains("Windows", ignoreCase = true),
    private val kernel32: Kernel32Ext = Kernel32Ext.INSTANCE,
) : MemoryReader {
    private var handle: HANDLE? = null
    private var mappedPointer: Pointer? = null

    override fun open(): Boolean {
        if (!isWindows) return false

        val h = kernel32.OpenFileMappingA(
            Kernel32Ext.FILE_MAP_READ,
            false,
            segmentName,
        ) ?: return false

        val ptr = kernel32.MapViewOfFile(
            h,
            Kernel32Ext.FILE_MAP_READ,
            0, 0,
            sizeBytes,
        )
        if (ptr == null) {
            kernel32.CloseHandle(h)
            return false
        }

        handle = h
        mappedPointer = ptr
        return true
    }

    override fun readBuffer(): ByteBuffer? {
        val ptr = mappedPointer ?: return null
        return ptr.getByteBuffer(0, sizeBytes.toLong()).order(ByteOrder.LITTLE_ENDIAN)
    }

    override fun isOpen(): Boolean = handle != null && mappedPointer != null

    override fun close() {
        mappedPointer?.let { kernel32.UnmapViewOfFile(it) }
        handle?.let { kernel32.CloseHandle(it) }
        mappedPointer = null
        handle = null
    }
}
