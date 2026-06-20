package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.repository.ProximityThresholdsPreferencesRepository

internal class FakeProximityThresholdsPreferencesRepository(
    initialLongitudinal: Double = 10.0,
    initialLateral: Double = 5.0,
) : ProximityThresholdsPreferencesRepository {
    private data class State(val longitudinal: Double, val lateral: Double)

    private val state = MutableStateFlow(State(initialLongitudinal, initialLateral))

    override fun observeLongitudinalThresholdMeters(): Flow<Double> = state.map { it.longitudinal }

    override fun observeLateralThresholdMeters(): Flow<Double> = state.map { it.lateral }

    override suspend fun saveLongitudinalThresholdMeters(meters: Double) {
        state.update { it.copy(longitudinal = meters) }
    }

    override suspend fun saveLateralThresholdMeters(meters: Double) {
        state.update { it.copy(lateral = meters) }
    }
}
