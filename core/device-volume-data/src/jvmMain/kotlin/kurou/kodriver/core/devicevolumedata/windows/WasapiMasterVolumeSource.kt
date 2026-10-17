package kurou.kodriver.core.devicevolumedata.windows

/** 既定の出力デバイスの音量操作とCOMリソースの解放を管理する。 */
internal class WasapiMasterVolumeSource(
    private val nativeApi: WasapiNativeApi = JnaWasapiNativeApi(),
) : WindowsMasterVolumeSource {
    override fun getScalarVolume(): Float = withAudioEndpointVolume { it.getMasterVolumeLevelScalar() }

    override fun setScalarVolume(level: Float) {
        withAudioEndpointVolume { it.setMasterVolumeLevelScalar(level) }
    }

    private fun <T> withAudioEndpointVolume(block: (WasapiEndpointVolume) -> T): T {
        val coInitResult = nativeApi.initialize()
        try {
            val enumerator = nativeApi.createDeviceEnumerator()
            try {
                val device = enumerator.getDefaultAudioEndpoint()
                try {
                    val endpointVolume = device.activate()
                    try {
                        return block(endpointVolume)
                    } finally {
                        endpointVolume.release()
                    }
                } finally {
                    device.release()
                }
            } finally {
                enumerator.release()
            }
        } finally {
            if (coInitResult) {
                nativeApi.uninitialize()
            }
        }
    }
}
