package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.lmuWindowsTyreTemperatureLowWarningSelectablePhases
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository

internal class FakeLmuWindowsTyreTemperaturePreferencesRepository(
    initialHighThreshold: Int = 90,
    initialLowWarningPhases: Map<SessionPhase, Boolean> = emptyMap(),
) : LmuWindowsTyreTemperaturePreferencesRepository {
    private val _highThreshold = MutableStateFlow(initialHighThreshold)
    private val _enabledStates = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
    private val _lowWarningPhases = MutableStateFlow(initialLowWarningPhases)

    override fun observeHighThresholdCelsius(): Flow<Int> = _highThreshold.asStateFlow()

    override suspend fun saveHighThresholdCelsius(celsius: Int) {
        _highThreshold.update { celsius }
    }

    override fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>> = _enabledStates.asStateFlow()

    override suspend fun saveEnabledState(key: ReadoutItemKey, enabled: Boolean) {
        _enabledStates.update { it + (key to enabled) }
    }

    override fun observeLowWarningPhases(): Flow<Map<SessionPhase, Boolean>> = _lowWarningPhases.asStateFlow()

    override suspend fun saveLowWarningPhases(phases: Set<SessionPhase>) {
        _lowWarningPhases.update {
            lmuWindowsTyreTemperatureLowWarningSelectablePhases.associateWith { phase -> phase in phases }
        }
    }
}
