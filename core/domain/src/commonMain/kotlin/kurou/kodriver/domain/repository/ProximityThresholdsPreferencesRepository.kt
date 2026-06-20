package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface ProximityThresholdsPreferencesRepository {
    fun observeLongitudinalThresholdMeters(): Flow<Double>
    fun observeLateralThresholdMeters(): Flow<Double>
    suspend fun saveLongitudinalThresholdMeters(meters: Double)
    suspend fun saveLateralThresholdMeters(meters: Double)
}
