package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.AceWindowsVehicleApproachPreferencesRepository

class AceWindowsVehicleApproachThresholdsUseCases(
    private val repository: AceWindowsVehicleApproachPreferencesRepository,
) {
    fun observeLongitudinalThresholdMeters(): Flow<Double> = repository.observeLongitudinalThresholdMeters()

    suspend fun saveLongitudinalThresholdMeters(meters: Double) = repository.saveLongitudinalThresholdMeters(meters)

    fun observeLateralThresholdMeters(): Flow<Double> = repository.observeLateralThresholdMeters()

    suspend fun saveLateralThresholdMeters(meters: Double) = repository.saveLateralThresholdMeters(meters)
}
