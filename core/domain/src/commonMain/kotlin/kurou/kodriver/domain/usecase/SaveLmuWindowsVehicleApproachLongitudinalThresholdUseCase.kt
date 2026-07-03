package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository

class SaveLmuWindowsVehicleApproachLongitudinalThresholdUseCase(
    private val repository: LmuWindowsVehicleApproachThresholdsPreferencesRepository,
) {
    suspend operator fun invoke(meters: Double) = repository.saveLongitudinalThresholdMeters(meters)
}
