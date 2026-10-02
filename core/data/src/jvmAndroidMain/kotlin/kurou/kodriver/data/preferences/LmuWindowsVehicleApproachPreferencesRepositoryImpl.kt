package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository

internal class LmuWindowsVehicleApproachPreferencesRepositoryImpl(
    private val dataStore: DataStore<LmuWindowsVehicleApproachPreferences>,
) : LmuWindowsVehicleApproachPreferencesRepository {
    override fun observeSkipFirstLap(): Flow<Boolean> = dataStore.observeProperty { it.skipFirstLap }

    override suspend fun saveSkipFirstLap(skip: Boolean) {
        dataStore.saveProperty(skip) { prefs, value -> prefs.copy(skipFirstLap = value) }
    }

    override fun observeStartLeftReadoutText(): Flow<String> = dataStore.observeProperty { it.startLeftReadoutText }

    override suspend fun saveStartLeftReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(startLeftReadoutText = value) }
    }

    override fun observeStartRightReadoutText(): Flow<String> = dataStore.observeProperty { it.startRightReadoutText }

    override suspend fun saveStartRightReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(startRightReadoutText = value) }
    }

    override fun observeSustainedLeftReadoutText(): Flow<String> =
        dataStore.observeProperty { it.sustainedLeftReadoutText }

    override suspend fun saveSustainedLeftReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(sustainedLeftReadoutText = value) }
    }

    override fun observeSustainedRightReadoutText(): Flow<String> =
        dataStore.observeProperty { it.sustainedRightReadoutText }

    override suspend fun saveSustainedRightReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(sustainedRightReadoutText = value) }
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
}
