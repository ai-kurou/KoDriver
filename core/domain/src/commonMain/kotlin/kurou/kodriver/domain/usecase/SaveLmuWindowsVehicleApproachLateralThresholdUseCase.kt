package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ProximityThresholdsPreferencesRepository

class SaveLmuWindowsVehicleApproachLateralThresholdUseCase(
    private val repository: ProximityThresholdsPreferencesRepository,
) {
    suspend operator fun invoke(meters: Double) = repository.saveLateralThresholdMeters(meters)
}
