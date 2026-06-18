package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.VehicleApproachPreferencesRepository

class SaveVehicleApproachStartReadoutEnabledUseCase(
    private val repository: VehicleApproachPreferencesRepository,
) {
    suspend operator fun invoke(enabled: Boolean) = repository.saveStartReadoutEnabled(enabled)
}
