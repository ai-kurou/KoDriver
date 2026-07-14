package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository

class SaveLmuWindowsVehicleApproachSustainedDurationUseCase(
    private val repository: LmuWindowsVehicleApproachThresholdsPreferencesRepository,
) {
    suspend operator fun invoke(seconds: Int) = repository.saveSustainedApproachDurationSeconds(seconds)
}
