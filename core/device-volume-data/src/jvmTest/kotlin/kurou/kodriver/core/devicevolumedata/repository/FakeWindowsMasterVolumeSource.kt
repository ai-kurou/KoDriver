package kurou.kodriver.core.devicevolumedata.repository

import kurou.kodriver.core.devicevolumedata.windows.WindowsMasterVolumeSource

class FakeWindowsMasterVolumeSource(
    private var scalarVolume: Float = 0f,
) : WindowsMasterVolumeSource {
    override fun getScalarVolume(): Float = scalarVolume

    override fun setScalarVolume(level: Float) {
        scalarVolume = level
    }
}
