package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.DeviceVolumeRepository

public class ObserveDeviceVolumeUseCase(
    private val repository: DeviceVolumeRepository,
) {
    public operator fun invoke(): Flow<Int> = repository.observeVolume()
}
