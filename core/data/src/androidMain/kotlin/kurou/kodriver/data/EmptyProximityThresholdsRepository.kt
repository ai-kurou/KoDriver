package kurou.kodriver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kurou.kodriver.domain.repository.ProximityThresholdsRepository

internal class EmptyProximityThresholdsRepository : ProximityThresholdsRepository {
    override fun observeLongitudinalThresholdMeters(): Flow<Double> = flowOf(1.0)
    override fun observeLateralThresholdMeters(): Flow<Double> = flowOf(2.0)
    override suspend fun saveLongitudinalThresholdMeters(meters: Double) = Unit
    override suspend fun saveLateralThresholdMeters(meters: Double) = Unit
}
