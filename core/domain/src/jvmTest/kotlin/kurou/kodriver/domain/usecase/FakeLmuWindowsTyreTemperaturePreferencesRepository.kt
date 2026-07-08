package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository

internal class FakeLmuWindowsTyreTemperaturePreferencesRepository(
    initialHighThreshold: Int = 90,
) : LmuWindowsTyreTemperaturePreferencesRepository {

    private val _highThreshold = MutableStateFlow(initialHighThreshold)
    private val _enabledStates = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())

    override fun observeHighThresholdCelsius(): Flow<Int> = _highThreshold.asStateFlow()

    override suspend fun saveHighThresholdCelsius(celsius: Int) {
        _highThreshold.update { celsius }
    }

    override fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>> = _enabledStates.asStateFlow()

    override suspend fun saveEnabledState(key: ReadoutItemKey, enabled: Boolean) {
        _enabledStates.update { it + (key to enabled) }
    }
}
