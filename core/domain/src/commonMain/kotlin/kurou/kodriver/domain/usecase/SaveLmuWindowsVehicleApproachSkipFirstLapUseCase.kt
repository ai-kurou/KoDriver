package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository

class SaveLmuWindowsVehicleApproachSkipFirstLapUseCase(
    private val repository: LmuWindowsVehicleApproachPreferencesRepository,
) {
    suspend operator fun invoke(skip: Boolean) = repository.saveSkipFirstLap(skip)
}
