package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsPitTimingPreferencesRepository

internal class LmuWindowsPitTimingPreferencesRepositoryImpl(
    private val dataStore: DataStore<LmuWindowsPitTimingPreferences>,
) : LmuWindowsPitTimingPreferencesRepository {
    override fun observeVirtualEnergyLaps(): Flow<Int> = dataStore.observeProperty { it.virtualEnergyLaps }

    override suspend fun saveVirtualEnergyLaps(laps: Int) {
        dataStore.saveProperty(laps) { prefs, value -> prefs.copy(virtualEnergyLaps = value) }
    }

    override fun observeTyreWearLaps(): Flow<Int> = dataStore.observeProperty { it.tyreWearLaps }

    override suspend fun saveTyreWearLaps(laps: Int) {
        dataStore.saveProperty(laps) { prefs, value -> prefs.copy(tyreWearLaps = value) }
    }

    override fun observeVirtualEnergyReadoutText(): Flow<String> =
        dataStore.observeProperty { it.virtualEnergyReadoutText }

    override suspend fun saveVirtualEnergyReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(virtualEnergyReadoutText = value) }
    }

    override fun observeVirtualEnergyImminentReadoutText(): Flow<String> =
        dataStore.observeProperty {
            it.virtualEnergyImminentReadoutText
        }

    override suspend fun saveVirtualEnergyImminentReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(virtualEnergyImminentReadoutText = value) }
    }

    override fun observeTyreWearReadoutText(): Flow<String> = dataStore.observeProperty { it.tyreWearReadoutText }

    override suspend fun saveTyreWearReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(tyreWearReadoutText = value) }
    }

    override fun observeTyreWearImminentReadoutText(): Flow<String> =
        dataStore.observeProperty {
            it.tyreWearImminentReadoutText
        }

    override suspend fun saveTyreWearImminentReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(tyreWearImminentReadoutText = value) }
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
