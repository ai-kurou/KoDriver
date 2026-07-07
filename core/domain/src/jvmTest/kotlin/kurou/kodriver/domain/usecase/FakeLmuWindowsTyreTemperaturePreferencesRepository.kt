package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository

internal class FakeLmuWindowsTyreTemperaturePreferencesRepository(
    initialHighThreshold: Int = 90,
    initialStates: Map<ReadoutItemKey, Boolean> = emptyMap(),
) : LmuWindowsTyreTemperaturePreferencesRepository {

    private val _highThreshold = MutableStateFlow(initialHighThreshold)
    private val states = MutableStateFlow(initialStates)

    override fun observeHighThresholdCelsius(): Flow<Int> = _highThreshold.asStateFlow()

    override suspend fun saveHighThresholdCelsius(celsius: Int) {
        _highThreshold.update { celsius }
    }

    override fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>> = states

    override suspend fun saveEnabledState(key: ReadoutItemKey, enabled: Boolean) {
        states.update { it + (key to enabled) }
    }
}
