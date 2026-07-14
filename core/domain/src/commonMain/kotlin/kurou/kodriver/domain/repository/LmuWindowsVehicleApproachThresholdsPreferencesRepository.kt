package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface LmuWindowsVehicleApproachThresholdsPreferencesRepository {
    fun observeLongitudinalThresholdMeters(): Flow<Double>
    fun observeLateralThresholdMeters(): Flow<Double>
    fun observeSustainedApproachDurationSeconds(): Flow<Int>
    suspend fun saveLongitudinalThresholdMeters(meters: Double)
    suspend fun saveLateralThresholdMeters(meters: Double)
    suspend fun saveSustainedApproachDurationSeconds(seconds: Int)
}
