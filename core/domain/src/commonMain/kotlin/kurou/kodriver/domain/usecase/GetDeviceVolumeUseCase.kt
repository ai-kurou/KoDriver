package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.DeviceVolumeRepository

public class GetDeviceVolumeUseCase(
    private val repository: DeviceVolumeRepository,
) {
    public suspend operator fun invoke(): Int = repository.getVolume()
}
