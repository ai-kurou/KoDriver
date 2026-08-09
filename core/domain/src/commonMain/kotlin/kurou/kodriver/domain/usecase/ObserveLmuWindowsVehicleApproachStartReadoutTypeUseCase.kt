package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository

class ObserveLmuWindowsVehicleApproachStartReadoutTypeUseCase(
    private val repository: LmuWindowsVehicleApproachPreferencesRepository,
) {
    operator fun invoke(): Flow<VehicleApproachStartReadoutType> = repository.observeStartReadoutType()
}
