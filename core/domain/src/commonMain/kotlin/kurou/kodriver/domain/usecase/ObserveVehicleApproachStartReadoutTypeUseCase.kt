package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.repository.VehicleApproachPreferencesRepository

class ObserveVehicleApproachStartReadoutTypeUseCase(
    private val repository: VehicleApproachPreferencesRepository,
) {
    operator fun invoke(): Flow<VehicleApproachStartReadoutType> = repository.observeStartReadoutType()
}
