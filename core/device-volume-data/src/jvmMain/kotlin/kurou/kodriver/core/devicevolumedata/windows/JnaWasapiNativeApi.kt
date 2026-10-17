package kurou.kodriver.core.devicevolumedata.windows

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.COM.COMUtils
import com.sun.jna.platform.win32.Guid.CLSID
import com.sun.jna.platform.win32.Guid.IID
import com.sun.jna.platform.win32.Ole32
import com.sun.jna.ptr.FloatByReference
import com.sun.jna.ptr.PointerByReference

private const val CLSCTX_ALL = 23
private const val E_RENDER = 0
private const val E_MULTIMEDIA = 1

private val CLSID_MM_DEVICE_ENUMERATOR = CLSID("BCDE0395-E52F-467C-8E3D-C4579291692E")
private val IID_MM_DEVICE_ENUMERATOR = IID("A95664D2-9614-4F35-A746-DE8DB63617E6")
private val IID_AUDIO_ENDPOINT_VOLUME = IID("5CDF2C82-841E-4546-9722-0CF74078229A")

/** WASAPIのCOM呼び出し引数と結果を組み立てる。ネイティブ呼び出しは境界を通して行う。 */
internal class JnaWasapiNativeApi(
    private val com: WasapiComBridge = JnaWasapiComBridge(),
) : WasapiNativeApi {
    override fun initialize(): Boolean = COMUtils.SUCCEEDED(com.initialize(Ole32.COINIT_APARTMENTTHREADED))

    override fun uninitialize() {
        com.uninitialize()
    }

    override fun createDeviceEnumerator(): WasapiDeviceEnumerator {
        val ppEnumerator = PointerByReference()
        val hr =
            com.createInstance(
                CLSID_MM_DEVICE_ENUMERATOR,
                CLSCTX_ALL,
                IID_MM_DEVICE_ENUMERATOR,
                ppEnumerator,
            )
        com.checkResult(hr)
        return MMDeviceEnumerator(ppEnumerator.value, com)
    }
}

/** `IMMDeviceEnumerator`のうち`GetDefaultAudioEndpoint`（vtableインデックス4）のみを扱う。 */
private class MMDeviceEnumerator(
    private val pointer: Pointer,
    private val com: WasapiComBridge,
) : WasapiDeviceEnumerator {
    override fun release() {
        com.release(pointer)
    }

    override fun getDefaultAudioEndpoint(): WasapiDevice {
        val ppDevice = PointerByReference()
        val hr =
            com.invoke(pointer, 4, arrayOf(pointer, E_RENDER, E_MULTIMEDIA, ppDevice))
        com.checkResult(hr)
        return MMDevice(ppDevice.value, com)
    }
}

/** `IMMDevice`のうち`Activate`（vtableインデックス3）のみを扱う。 */
private class MMDevice(
    private val pointer: Pointer,
    private val com: WasapiComBridge,
) : WasapiDevice {
    override fun release() {
        com.release(pointer)
    }

    override fun activate(): WasapiEndpointVolume {
        val ppInterface = PointerByReference()
        val hr =
            com.invoke(
                pointer,
                3,
                arrayOf(pointer, IID_AUDIO_ENDPOINT_VOLUME, CLSCTX_ALL, null, ppInterface),
            )
        com.checkResult(hr)
        return AudioEndpointVolume(ppInterface.value, com)
    }
}

/**
 * `IAudioEndpointVolume`のうち`SetMasterVolumeLevelScalar`（vtableインデックス7）・
 * `GetMasterVolumeLevelScalar`（vtableインデックス9）のみを扱う。
 */
private class AudioEndpointVolume(
    private val pointer: Pointer,
    private val com: WasapiComBridge,
) : WasapiEndpointVolume {
    override fun release() {
        com.release(pointer)
    }

    override fun getMasterVolumeLevelScalar(): Float {
        val level = FloatByReference()
        val hr = com.invoke(pointer, 9, arrayOf(pointer, level))
        com.checkResult(hr)
        return level.value
    }

    override fun setMasterVolumeLevelScalar(level: Float) {
        val hr = com.invoke(pointer, 7, arrayOf(pointer, level, null))
        com.checkResult(hr)
    }
}
