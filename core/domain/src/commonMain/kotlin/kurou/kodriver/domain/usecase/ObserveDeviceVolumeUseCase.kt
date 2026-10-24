package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.DeviceVolumeRepository

class ObserveDeviceVolumeUseCase(
    private val repository: DeviceVolumeRepository,
) {
    operator fun invoke(): Flow<Int> = repository.observeVolume()
}
