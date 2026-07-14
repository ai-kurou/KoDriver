package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface LmuWindowsVehicleApproachThresholdsPreferencesRepository {
    fun observeLongitudinalThresholdMeters(): Flow<Double>
    fun observeLateralThresholdMeters(): Flow<Double>
    fun observeSustainedApproachDurationSeconds(): Flow<Int>
    fun observeSustainedApproachEnabled(): Flow<Boolean>
    suspend fun saveLongitudinalThresholdMeters(meters: Double)
    suspend fun saveLateralThresholdMeters(meters: Double)
    suspend fun saveSustainedApproachDurationSeconds(seconds: Int)
    suspend fun saveSustainedApproachEnabled(enabled: Boolean)
}
