package kurou.kodriver.core.devicevolumedata.windows

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.Guid.CLSID
import com.sun.jna.platform.win32.Guid.IID
import com.sun.jna.platform.win32.WinNT.HRESULT
import com.sun.jna.ptr.PointerByReference

/** COMの実呼び出しとWindows固有のエラー解決を隔離する境界。 */
internal interface WasapiComBridge {
    fun initialize(flags: Int): HRESULT

    fun uninitialize()

    fun createInstance(
        classId: CLSID,
        context: Int,
        interfaceId: IID,
        result: PointerByReference,
    ): HRESULT

    fun invoke(
        pointer: Pointer,
        vtableIndex: Int,
        arguments: Array<Any?>,
    ): HRESULT

    fun release(pointer: Pointer)

    fun checkResult(result: HRESULT)
}
