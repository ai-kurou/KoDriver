package kurou.kodriver.core.devicevolumedata.windows

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.Guid.CLSID
import com.sun.jna.platform.win32.Guid.IID
import com.sun.jna.platform.win32.WinNT.HRESULT
import com.sun.jna.ptr.FloatByReference
import com.sun.jna.ptr.PointerByReference

internal class FakeWasapiComBridge : WasapiComBridge {
    val enumeratorPointer = Pointer(0x100L)
    val devicePointer = Pointer(0x200L)
    val volumePointer = Pointer(0x300L)
    val failure = IllegalStateException("COM call failed")
    var initializeResult = HRESULT(0)
    var initializeFlags: Int? = null
    var uninitializeCount = 0
    var creation: Triple<CLSID, Int, IID>? = null
    val invocations = mutableListOf<Invocation>()
    val releasedPointers = mutableListOf<Pointer>()
    val checkedResults = mutableListOf<HRESULT>()
    var failingCall: String? = null
    var scalarVolume = 0.75f

    override fun initialize(flags: Int): HRESULT {
        initializeFlags = flags
        return initializeResult
    }

    override fun uninitialize() {
        uninitializeCount++
    }

    override fun createInstance(
        classId: CLSID,
        context: Int,
        interfaceId: IID,
        result: PointerByReference,
    ): HRESULT {
        creation = Triple(classId, context, interfaceId)
        result.value = enumeratorPointer
        return HRESULT(if (failingCall == "create") -1 else 0)
    }

    override fun invoke(
        pointer: Pointer,
        vtableIndex: Int,
        arguments: Array<Any?>,
    ): HRESULT {
        invocations.add(Invocation(pointer, vtableIndex, arguments.toList()))
        when (vtableIndex) {
            4 -> (arguments[3] as PointerByReference).value = devicePointer
            3 -> (arguments[4] as PointerByReference).value = volumePointer
            9 -> (arguments[1] as FloatByReference).value = scalarVolume
        }
        return HRESULT(if (failingCall == vtableIndex.toString()) -1 else 0)
    }

    override fun release(pointer: Pointer) {
        releasedPointers.add(pointer)
    }

    override fun checkResult(result: HRESULT) {
        checkedResults.add(result)
        if (result.toInt() < 0) throw failure
    }

    data class Invocation(
        val pointer: Pointer,
        val index: Int,
        val arguments: List<Any?>,
    )
}
