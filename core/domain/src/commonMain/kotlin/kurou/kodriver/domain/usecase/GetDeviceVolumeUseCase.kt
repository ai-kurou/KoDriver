package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.DeviceVolumeRepository

class GetDeviceVolumeUseCase(
    private val repository: DeviceVolumeRepository,
) {
    suspend operator fun invoke(): Int = repository.getVolume()
}
