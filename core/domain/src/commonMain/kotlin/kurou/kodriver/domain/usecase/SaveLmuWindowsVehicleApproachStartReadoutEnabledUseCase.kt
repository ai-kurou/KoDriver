package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository

class SaveLmuWindowsVehicleApproachStartReadoutEnabledUseCase(
    private val repository: LmuWindowsVehicleApproachPreferencesRepository,
) {
    suspend operator fun invoke(enabled: Boolean) = repository.saveStartReadoutEnabled(enabled)
}
