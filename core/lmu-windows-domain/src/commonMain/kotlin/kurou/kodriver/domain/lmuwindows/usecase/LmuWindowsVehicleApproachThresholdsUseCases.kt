package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository

public class LmuWindowsVehicleApproachThresholdsUseCases(
    private val repository: LmuWindowsVehicleApproachThresholdsPreferencesRepository,
) {
    public fun observeLongitudinalThresholdMeters(): Flow<Double> = repository.observeLongitudinalThresholdMeters()

    public suspend fun saveLongitudinalThresholdMeters(meters: Double): Unit =
        repository.saveLongitudinalThresholdMeters(meters)

    public fun observeLateralThresholdMeters(): Flow<Double> = repository.observeLateralThresholdMeters()

    public suspend fun saveLateralThresholdMeters(meters: Double): Unit = repository.saveLateralThresholdMeters(meters)

    public fun observeSustainedApproachDurationSeconds(): Flow<Int> =
        repository.observeSustainedApproachDurationSeconds()

    public suspend fun saveSustainedApproachDurationSeconds(seconds: Int): Unit =
        repository.saveSustainedApproachDurationSeconds(seconds)
}
