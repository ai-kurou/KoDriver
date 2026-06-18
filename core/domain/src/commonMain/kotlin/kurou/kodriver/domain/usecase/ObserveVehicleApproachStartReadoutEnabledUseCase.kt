package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.VehicleApproachPreferencesRepository

class ObserveVehicleApproachStartReadoutEnabledUseCase(
    private val repository: VehicleApproachPreferencesRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.observeStartReadoutEnabled()
}
