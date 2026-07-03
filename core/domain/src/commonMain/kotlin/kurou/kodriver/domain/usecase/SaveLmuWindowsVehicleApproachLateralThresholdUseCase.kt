package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository

class SaveLmuWindowsVehicleApproachLateralThresholdUseCase(
    private val repository: LmuWindowsVehicleApproachThresholdsPreferencesRepository,
) {
    suspend operator fun invoke(meters: Double) = repository.saveLateralThresholdMeters(meters)
}
