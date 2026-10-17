package kurou.kodriver.core.devicevolumedata.windows

/** COMの初期化とオーディオデバイスへのアクセス。テストではFakeに差し替える。 */
internal interface WasapiNativeApi {
    /** 初期化が成功し、対応する終了処理が必要な場合にtrueを返す。 */
    fun initialize(): Boolean

    fun uninitialize()

    fun createDeviceEnumerator(): WasapiDeviceEnumerator
}

internal interface WasapiDeviceEnumerator {
    fun getDefaultAudioEndpoint(): WasapiDevice

    fun release()
}

internal interface WasapiDevice {
    fun activate(): WasapiEndpointVolume

    fun release()
}

internal interface WasapiEndpointVolume {
    fun getMasterVolumeLevelScalar(): Float

    fun setMasterVolumeLevelScalar(level: Float)

    fun release()
}
