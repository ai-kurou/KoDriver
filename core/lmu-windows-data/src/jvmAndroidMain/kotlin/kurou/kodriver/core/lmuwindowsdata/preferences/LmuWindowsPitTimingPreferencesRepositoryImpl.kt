package kurou.kodriver.core.lmuwindowsdata.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitTimingPreferencesRepository
import kurou.kodriver.domain.model.ReadoutItemKey

internal class LmuWindowsPitTimingPreferencesRepositoryImpl(
    private val dataStore: DataStore<LmuWindowsPitTimingPreferences>,
) : LmuWindowsPitTimingPreferencesRepository {
    override fun observeVirtualEnergyLaps(): Flow<Int> = dataStore.observeProperty { it.virtualEnergyLaps }

    override suspend fun saveVirtualEnergyLaps(laps: Int) {
        dataStore.saveProperty(laps) { prefs, value -> prefs.copy(virtualEnergyLaps = value) }
    }

    override fun observeTyreWearLaps(): Flow<Int> = dataStore.observeProperty { it.tyreWearLaps }

    override fun observeFuelLaps(): Flow<Int> = dataStore.observeProperty { it.fuelLaps }

    override suspend fun saveTyreWearLaps(laps: Int) {
        dataStore.saveProperty(laps) { prefs, value -> prefs.copy(tyreWearLaps = value) }
    }

    override suspend fun saveFuelLaps(laps: Int) {
        dataStore.saveProperty(laps) { prefs, value -> prefs.copy(fuelLaps = value) }
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
                enabledStates = prefs.enabledStates + (key.value to value),
            )
        }
    }
}
