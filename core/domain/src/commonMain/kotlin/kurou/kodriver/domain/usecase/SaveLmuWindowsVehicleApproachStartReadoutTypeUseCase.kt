package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository

class SaveLmuWindowsVehicleApproachStartReadoutTypeUseCase(
    private val repository: LmuWindowsVehicleApproachPreferencesRepository,
) {
    suspend operator fun invoke(type: VehicleApproachStartReadoutType) = repository.saveStartReadoutType(type)
}
