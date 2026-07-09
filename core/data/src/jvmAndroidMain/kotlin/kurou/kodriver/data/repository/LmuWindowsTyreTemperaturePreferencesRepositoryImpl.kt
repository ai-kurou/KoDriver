package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.data.model.LmuWindowsTyreTemperaturePreferences
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository

private val defaultLowWarningPhases: Set<SessionPhase> = setOf(
    SessionPhase.GARAGE,
    SessionPhase.WARM_UP,
    SessionPhase.GRID_WALK,
    SessionPhase.FORMATION,
)

internal class LmuWindowsTyreTemperaturePreferencesRepositoryImpl(
    private val dataStore: DataStore<LmuWindowsTyreTemperaturePreferences>,
) : LmuWindowsTyreTemperaturePreferencesRepository {

    override fun observeHighThresholdCelsius(): Flow<Int> =
        dataStore.data.map { it.highThresholdCelsius }

    override suspend fun saveHighThresholdCelsius(celsius: Int) {
        dataStore.updateData { it.copy(highThresholdCelsius = celsius) }
    }

    override fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>> =
        dataStore.data.map { prefs ->
            prefs.enabledStates
                .mapNotNull { (key, enabled) -> ReadoutItemKey.fromValue(key)?.let { it to enabled } }
                .toMap()
        }

    override suspend fun saveEnabledState(key: ReadoutItemKey, enabled: Boolean) {
        dataStore.updateData { it.copy(enabledStates = it.enabledStates + (key.value to enabled)) }
    }

    override fun observeLowWarningPhases(): Flow<Set<SessionPhase>> =
        dataStore.data.map { prefs ->
            if (prefs.lowWarningPhases.isEmpty()) {
                defaultLowWarningPhases
            } else {
                prefs.lowWarningPhases
                    .filterValues { enabled -> enabled }
                    .keys
                    .map { SessionPhase.fromRaw(it) }
                    .filter { it != SessionPhase.UNKNOWN }
                    .toSet()
            }
        }

    override suspend fun saveLowWarningPhases(phases: Set<SessionPhase>) {
        val explicitPhases = defaultLowWarningPhases.associate { phase -> phase.rawValue to (phase in phases) }
        dataStore.updateData { it.copy(lowWarningPhases = explicitPhases) }
    }
}
