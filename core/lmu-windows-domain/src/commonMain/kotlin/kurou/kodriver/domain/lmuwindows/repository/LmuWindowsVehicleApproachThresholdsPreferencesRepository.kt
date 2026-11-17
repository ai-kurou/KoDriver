package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow

public interface LmuWindowsVehicleApproachThresholdsPreferencesRepository {
    public suspend fun resetToDefault()

    public fun observeLongitudinalThresholdMeters(): Flow<Double>

    public fun observeLateralThresholdMeters(): Flow<Double>

    public fun observeSustainedApproachDurationSeconds(): Flow<Int>

    public suspend fun saveLongitudinalThresholdMeters(meters: Double)

    public suspend fun saveLateralThresholdMeters(meters: Double)

    public suspend fun saveSustainedApproachDurationSeconds(seconds: Int)
}
