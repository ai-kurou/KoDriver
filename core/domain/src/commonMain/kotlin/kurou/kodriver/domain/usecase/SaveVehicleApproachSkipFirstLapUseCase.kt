package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.VehicleApproachPreferencesRepository

class SaveVehicleApproachSkipFirstLapUseCase(
    private val repository: VehicleApproachPreferencesRepository,
) {
    suspend operator fun invoke(skip: Boolean) = repository.saveSkipFirstLap(skip)
}
