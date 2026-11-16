package kurou.kodriver.core.windowssharedmemory.datasource

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinNT.HANDLE

/**
 * Windows file mapping API へアクセスする JNA interface。
 */
@Suppress("FunctionNaming", "ktlint:standard:function-naming")
public interface Kernel32FileMapping : Library {
    public companion object {
        public val INSTANCE: Kernel32FileMapping by lazy {
            Native.load("kernel32", Kernel32FileMapping::class.java)
        }
        public const val FILE_MAP_READ: Int = 0x0004
    }

    public fun OpenFileMappingA(
        dwDesiredAccess: Int,
        bInheritHandle: Boolean,
        lpName: String,
    ): HANDLE?

    public fun MapViewOfFile(
        hFileMappingObject: HANDLE,
        dwDesiredAccess: Int,
        dwFileOffsetHigh: Int,
        dwFileOffsetLow: Int,
        dwNumberOfBytesToMap: Int,
    ): Pointer?

    public fun UnmapViewOfFile(lpBaseAddress: Pointer): Boolean

    public fun CloseHandle(hObject: HANDLE): Boolean
}
