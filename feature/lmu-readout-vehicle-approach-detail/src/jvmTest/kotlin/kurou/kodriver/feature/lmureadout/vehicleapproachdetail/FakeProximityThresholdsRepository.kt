package kurou.kodriver.feature.lmureadout.vehicleapproachdetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.repository.ProximityThresholdsRepository

internal class FakeProximityThresholdsRepository(
    initialLateral: Double = 5.0,
    initialLongitudinal: Double = 1.0,
) : ProximityThresholdsRepository {
    private data class State(val lateral: Double, val longitudinal: Double)

    private val state = MutableStateFlow(State(initialLateral, initialLongitudinal))

    override fun observeLateralThresholdMeters(): Flow<Double> = state.map { it.lateral }

    override fun observeLongitudinalThresholdMeters(): Flow<Double> = state.map { it.longitudinal }

    override suspend fun saveLateralThresholdMeters(meters: Double) {
        state.update { it.copy(lateral = meters) }
    }

    override suspend fun saveLongitudinalThresholdMeters(meters: Double) {
        state.update { it.copy(longitudinal = meters) }
    }
}
