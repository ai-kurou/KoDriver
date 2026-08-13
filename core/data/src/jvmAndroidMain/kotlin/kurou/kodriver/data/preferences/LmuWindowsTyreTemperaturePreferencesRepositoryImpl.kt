package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.lmuWindowsTyreTemperatureLowWarningSelectablePhases
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository

internal class LmuWindowsTyreTemperaturePreferencesRepositoryImpl(
    private val dataStore: DataStore<LmuWindowsTyreTemperaturePreferences>,
) : LmuWindowsTyreTemperaturePreferencesRepository {
    override fun observeHighThresholdCelsius(): Flow<Int> = dataStore.observeProperty { it.highThresholdCelsius }

    override suspend fun saveHighThresholdCelsius(celsius: Int) {
        dataStore.saveProperty(celsius) { prefs, value -> prefs.copy(highThresholdCelsius = value) }
    }

    override fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>> =
        dataStore.observeProperty { prefs ->
            prefs.enabledStates
                .mapNotNull { (key, enabled) -> ReadoutItemKey.fromValue(key)?.let { it to enabled } }
                .toMap()
        }

    override suspend fun saveEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    ) {
        dataStore.saveProperty(enabled) { prefs, value ->
            prefs.copy(
                enabledStates =
                    prefs.enabledStates + (key.value to value),
            )
        }
    }

    override fun observeLowWarningPhases(): Flow<Map<SessionPhase, Boolean>> =
        dataStore.observeProperty { prefs ->
            prefs.lowWarningPhases
                .mapNotNull { (raw, enabled) ->
                    SessionPhase.fromRaw(raw).takeIf { it != SessionPhase.UNKNOWN }?.let { it to enabled }
                }.toMap()
        }

    override suspend fun saveLowWarningPhases(phases: Set<SessionPhase>) {
        val explicitPhases =
            lmuWindowsTyreTemperatureLowWarningSelectablePhases
                .associate { phase -> phase.rawValue to (phase in phases) }
        dataStore.saveProperty(explicitPhases) { prefs, value -> prefs.copy(lowWarningPhases = value) }
    }
}
