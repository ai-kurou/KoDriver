package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.repository.VehicleApproachPreferencesRepository

class SaveLmuWindowsVehicleApproachStartReadoutTypeUseCase(
    private val repository: VehicleApproachPreferencesRepository,
) {
    suspend operator fun invoke(type: VehicleApproachStartReadoutType) = repository.saveStartReadoutType(type)
}
