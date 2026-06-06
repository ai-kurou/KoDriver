package kurou.kodriver.data.datasource

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinNT.HANDLE

@Suppress("FunctionNaming")
internal interface Kernel32Ext : Library {
    companion object {
        val INSTANCE: Kernel32Ext by lazy {
            Native.load("kernel32", Kernel32Ext::class.java)
        }
        const val FILE_MAP_READ = 0x0004
    }

    fun OpenFileMappingA(dwDesiredAccess: Int, bInheritHandle: Boolean, lpName: String): HANDLE?

    fun MapViewOfFile(
        hFileMappingObject: HANDLE,
        dwDesiredAccess: Int,
        dwFileOffsetHigh: Int,
        dwFileOffsetLow: Int,
        dwNumberOfBytesToMap: Int,
    ): Pointer?

    fun UnmapViewOfFile(lpBaseAddress: Pointer): Boolean
    fun CloseHandle(hObject: HANDLE): Boolean
}
