package kurou.kodriver.core.devicevolumedata.windows

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.COM.COMUtils
import com.sun.jna.platform.win32.COM.Unknown
import com.sun.jna.platform.win32.Guid.CLSID
import com.sun.jna.platform.win32.Guid.IID
import com.sun.jna.platform.win32.Ole32
import com.sun.jna.platform.win32.WinNT.HRESULT
import com.sun.jna.ptr.FloatByReference
import com.sun.jna.ptr.PointerByReference

private const val CLSCTX_ALL = 23
private const val E_RENDER = 0
private const val E_MULTIMEDIA = 1

private val CLSID_MM_DEVICE_ENUMERATOR = CLSID("BCDE0395-E52F-467C-8E3D-C4579291692E")
private val IID_MM_DEVICE_ENUMERATOR = IID("A95664D2-9614-4F35-A746-DE8DB63617E6")
private val IID_AUDIO_ENDPOINT_VOLUME = IID("5CDF2C82-841E-4546-9722-0CF74078229A")

/**
 * WASAPI（`IAudioEndpointVolume`）をJNA経由で直接呼び出し、既定のオーディオ出力デバイスの
 * マスター音量を取得・設定する。プラットフォーム固有の外部APIを直接呼び出すためユニットテスト
 * 対象外とし（CLAUDE.mdのテスト方針を参照）、上位の [kurou.kodriver.core.devicevolumedata.repository.WindowsDeviceVolumeRepository]
 * 側のロジックを [WindowsMasterVolumeSource] 経由のFakeでテストする。
 */
internal class WasapiMasterVolumeSource : WindowsMasterVolumeSource {
    override fun getScalarVolume(): Float = withAudioEndpointVolume { it.getMasterVolumeLevelScalar() }

    override fun setScalarVolume(level: Float) {
        withAudioEndpointVolume { it.setMasterVolumeLevelScalar(level) }
    }

    private fun <T> withAudioEndpointVolume(block: (AudioEndpointVolume) -> T): T {
        val coInitResult = Ole32.INSTANCE.CoInitializeEx(null, Ole32.COINIT_APARTMENTTHREADED)
        try {
            val enumerator = createDeviceEnumerator()
            try {
                val device = MMDevice(enumerator.getDefaultAudioEndpoint())
                try {
                    val endpointVolume = AudioEndpointVolume(device.activate())
                    try {
                        return block(endpointVolume)
                    } finally {
                        endpointVolume.Release()
                    }
                } finally {
                    device.Release()
                }
            } finally {
                enumerator.Release()
            }
        } finally {
            if (COMUtils.SUCCEEDED(coInitResult)) {
                Ole32.INSTANCE.CoUninitialize()
            }
        }
    }

    private fun createDeviceEnumerator(): MMDeviceEnumerator {
        val ppEnumerator = PointerByReference()
        val hr =
            Ole32.INSTANCE.CoCreateInstance(
                CLSID_MM_DEVICE_ENUMERATOR,
                null,
                CLSCTX_ALL,
                IID_MM_DEVICE_ENUMERATOR,
                ppEnumerator,
            )
        COMUtils.checkRC(hr)
        return MMDeviceEnumerator(ppEnumerator.value)
    }
}

/** `IMMDeviceEnumerator`のうち`GetDefaultAudioEndpoint`（vtableインデックス4）のみを扱う。 */
private class MMDeviceEnumerator(
    pointer: Pointer,
) : Unknown(pointer) {
    fun getDefaultAudioEndpoint(): Pointer {
        val ppDevice = PointerByReference()
        val hr =
            _invokeNativeObject(4, arrayOf(pointer, E_RENDER, E_MULTIMEDIA, ppDevice), HRESULT::class.java) as HRESULT
        COMUtils.checkRC(hr)
        return ppDevice.value
    }
}

/** `IMMDevice`のうち`Activate`（vtableインデックス3）のみを扱う。 */
private class MMDevice(
    pointer: Pointer,
) : Unknown(pointer) {
    fun activate(): Pointer {
        val ppInterface = PointerByReference()
        val hr =
            _invokeNativeObject(
                3,
                arrayOf(pointer, IID_AUDIO_ENDPOINT_VOLUME, CLSCTX_ALL, null, ppInterface),
                HRESULT::class.java,
            ) as HRESULT
        COMUtils.checkRC(hr)
        return ppInterface.value
    }
}

/**
 * `IAudioEndpointVolume`のうち`SetMasterVolumeLevelScalar`（vtableインデックス7）・
 * `GetMasterVolumeLevelScalar`（vtableインデックス9）のみを扱う。
 */
private class AudioEndpointVolume(
    pointer: Pointer,
) : Unknown(pointer) {
    fun getMasterVolumeLevelScalar(): Float {
        val level = FloatByReference()
        val hr = _invokeNativeObject(9, arrayOf(pointer, level), HRESULT::class.java) as HRESULT
        COMUtils.checkRC(hr)
        return level.value
    }

    fun setMasterVolumeLevelScalar(level: Float) {
        val hr = _invokeNativeObject(7, arrayOf(pointer, level, null), HRESULT::class.java) as HRESULT
        COMUtils.checkRC(hr)
    }
}
