package kurou.kodriver.core.devicevolumedata.windows

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.COM.COMUtils
import com.sun.jna.platform.win32.COM.Unknown
import com.sun.jna.platform.win32.Guid.CLSID
import com.sun.jna.platform.win32.Guid.IID
import com.sun.jna.platform.win32.Ole32
import com.sun.jna.platform.win32.WinNT.HRESULT
import com.sun.jna.ptr.PointerByReference

/** JNAの実呼び出しを担う。Ole32は呼び出し時に取得し、非Windowsでも境界を検証できる。 */
internal class JnaWasapiComBridge(
    private val ole32: () -> Ole32 = { Ole32.INSTANCE },
) : WasapiComBridge {
    override fun initialize(flags: Int): HRESULT = ole32().CoInitializeEx(null, flags)

    override fun uninitialize() {
        ole32().CoUninitialize()
    }

    override fun createInstance(
        classId: CLSID,
        context: Int,
        interfaceId: IID,
        result: PointerByReference,
    ): HRESULT = ole32().CoCreateInstance(classId, null, context, interfaceId, result)

    override fun invoke(
        pointer: Pointer,
        vtableIndex: Int,
        arguments: Array<Any?>,
    ): HRESULT = WasapiComInvoker(pointer).invoke(vtableIndex, arguments)

    override fun release(pointer: Pointer) {
        Unknown(pointer).Release()
    }

    override fun checkResult(result: HRESULT) {
        COMUtils.checkRC(result)
    }
}

/** JNAのprotectedなvtable呼び出しを境界の実装から利用する。 */
private class WasapiComInvoker(
    pointer: Pointer,
) : Unknown(pointer) {
    fun invoke(
        vtableIndex: Int,
        arguments: Array<Any?>,
    ): HRESULT = _invokeNativeObject(vtableIndex, arguments, HRESULT::class.java) as HRESULT
}
