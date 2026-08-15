package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.DEVICE_VOLUME_MAX
import kurou.kodriver.domain.model.DEVICE_VOLUME_MIN
import kurou.kodriver.domain.repository.DeviceVolumeRepository

class SetDeviceVolumeUseCase(
    private val repository: DeviceVolumeRepository,
) {
    suspend operator fun invoke(volume: Int) {
        require(volume in DEVICE_VOLUME_MIN..DEVICE_VOLUME_MAX) { "volume must be between 0 and 100" }
        repository.setVolume(volume)
    }
}
