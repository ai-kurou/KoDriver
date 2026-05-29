package kurou.kodriver.data.datasource

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinNT.HANDLE
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal class Rf2SharedMemoryReader(
    private val segmentName: String,
    private val sizeBytes: Int,
) {
    private var handle: HANDLE? = null
    private var mappedPointer: Pointer? = null

    companion object {
        const val RF2_TELEMETRY_SEGMENT = "rFactor2SMMP_Telemetry"
        const val RF2_SCORING_SEGMENT = "rFactor2SMMP_Scoring"
        const val RF2_TELEMETRY_SIZE = 524_288
        const val RF2_SCORING_SIZE = 524_288
    }

    fun open(): Boolean {
        if (!System.getProperty("os.name").contains("Windows", ignoreCase = true)) return false

        val h = Kernel32Ext.INSTANCE.OpenFileMappingA(
            Kernel32Ext.FILE_MAP_READ,
            false,
            segmentName,
        ) ?: return false

        val ptr = Kernel32Ext.INSTANCE.MapViewOfFile(
            h,
            Kernel32Ext.FILE_MAP_READ,
            0, 0,
            sizeBytes,
        )
        if (ptr == null) {
            Kernel32Ext.INSTANCE.CloseHandle(h)
            return false
        }

        handle = h
        mappedPointer = ptr
        return true
    }

    fun readBuffer(): ByteBuffer? {
        val ptr = mappedPointer ?: return null
        return ptr.getByteBuffer(0, sizeBytes.toLong()).order(ByteOrder.LITTLE_ENDIAN)
    }

    fun isOpen(): Boolean = handle != null && mappedPointer != null

    fun close() {
        mappedPointer?.let { Kernel32Ext.INSTANCE.UnmapViewOfFile(it) }
        handle?.let { Kernel32Ext.INSTANCE.CloseHandle(it) }
        mappedPointer = null
        handle = null
    }
}
