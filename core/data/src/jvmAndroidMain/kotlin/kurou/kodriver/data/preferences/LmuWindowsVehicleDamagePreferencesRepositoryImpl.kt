package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository

internal class LmuWindowsVehicleDamagePreferencesRepositoryImpl(
    private val dataStore: DataStore<LmuWindowsVehicleDamagePreferences>,
) : LmuWindowsVehicleDamagePreferencesRepository {
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

    override fun observeOverheatReadoutText(): Flow<String> = dataStore.observeProperty { it.overheatReadoutText }

    override suspend fun saveOverheatReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(overheatReadoutText = value) }
    }

    override fun observePartDetachedReadoutText(): Flow<String> =
        dataStore.observeProperty { it.partDetachedReadoutText }

    override suspend fun savePartDetachedReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(partDetachedReadoutText = value) }
    }

    override fun observeTyreDetachedReadoutText(): Flow<String> =
        dataStore.observeProperty { it.tyreDetachedReadoutText }

    override suspend fun saveTyreDetachedReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(tyreDetachedReadoutText = value) }
    }
}
