package kurou.kodriver.core.windowssharedmemory.datasource

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinNT.HANDLE

internal class FakeKernel32FileMapping(
    private val openFileMappingResult: HANDLE? = HANDLE(Pointer.createConstant(1L)),
    private val mapViewOfFileResult: Pointer? = Pointer.createConstant(2L),
) : Kernel32FileMapping {
    var openFileMappingCallCount = 0
    var mapViewOfFileCallCount = 0
    var closeHandleCallCount = 0
    var unmapViewOfFileCalled = false

    override fun OpenFileMappingA(dwDesiredAccess: Int, bInheritHandle: Boolean, lpName: String): HANDLE? {
        openFileMappingCallCount++
        return openFileMappingResult
    }

    override fun MapViewOfFile(
        hFileMappingObject: HANDLE,
        dwDesiredAccess: Int,
        dwFileOffsetHigh: Int,
        dwFileOffsetLow: Int,
        dwNumberOfBytesToMap: Int,
    ): Pointer? {
        mapViewOfFileCallCount++
        return mapViewOfFileResult
    }

    override fun UnmapViewOfFile(lpBaseAddress: Pointer): Boolean {
        unmapViewOfFileCalled = true
        return true
    }

    override fun CloseHandle(hObject: HANDLE): Boolean {
        closeHandleCallCount++
        return true
    }
}
