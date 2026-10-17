package kurou.kodriver.core.devicevolumedata.windows

internal class FakeWasapiNativeApi(
    private val initialized: Boolean = true,
    private val failingCall: String? = null,
) : WasapiNativeApi {
    val calls = mutableListOf<String>()
    val failure = IllegalStateException("WASAPI failure")
    var scalarVolume = 0.5f
        private set

    private fun record(call: String) {
        calls.add(call)
        if (call == failingCall) throw failure
    }

    override fun initialize(): Boolean {
        record("initialize")
        return initialized
    }

    override fun uninitialize() = record("uninitialize")

    override fun createDeviceEnumerator(): WasapiDeviceEnumerator {
        record("create")
        return object : WasapiDeviceEnumerator {
            override fun getDefaultAudioEndpoint(): WasapiDevice {
                record("device")
                return object : WasapiDevice {
                    override fun activate(): WasapiEndpointVolume {
                        record("activate")
                        return object : WasapiEndpointVolume {
                            override fun getMasterVolumeLevelScalar(): Float {
                                record("get")
                                return scalarVolume
                            }

                            override fun setMasterVolumeLevelScalar(level: Float) {
                                record("set")
                                scalarVolume = level
                            }

                            override fun release() = record("releaseEndpoint")
                        }
                    }

                    override fun release() = record("releaseDevice")
                }
            }

            override fun release() = record("releaseEnumerator")
        }
    }
}
