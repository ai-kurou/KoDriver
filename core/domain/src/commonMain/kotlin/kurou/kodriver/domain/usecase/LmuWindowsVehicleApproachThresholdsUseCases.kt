package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository

class LmuWindowsVehicleApproachThresholdsUseCases(
    private val repository: LmuWindowsVehicleApproachThresholdsPreferencesRepository,
) {
    fun observeLongitudinalThresholdMeters(): Flow<Double> = repository.observeLongitudinalThresholdMeters()

    suspend fun saveLongitudinalThresholdMeters(meters: Double) = repository.saveLongitudinalThresholdMeters(meters)

    fun observeLateralThresholdMeters(): Flow<Double> = repository.observeLateralThresholdMeters()

    suspend fun saveLateralThresholdMeters(meters: Double) = repository.saveLateralThresholdMeters(meters)

    fun observeSustainedApproachDurationSeconds(): Flow<Int> = repository.observeSustainedApproachDurationSeconds()

    suspend fun saveSustainedApproachDurationSeconds(seconds: Int) =
        repository.saveSustainedApproachDurationSeconds(seconds)

    fun observeSustainedApproachEnabled(): Flow<Boolean> = repository.observeSustainedApproachEnabled()

    suspend fun saveSustainedApproachEnabled(enabled: Boolean) = repository.saveSustainedApproachEnabled(enabled)
}
