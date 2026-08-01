package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.data.model.LmuWindowsVehicleDamagePreferences
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository

internal class LmuWindowsVehicleDamagePreferencesRepositoryImpl(
    private val dataStore: DataStore<LmuWindowsVehicleDamagePreferences>,
) : LmuWindowsVehicleDamagePreferencesRepository {
    override fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>> =
        dataStore.data.map { prefs ->
            prefs.enabledStates
                .mapNotNull { (key, enabled) -> ReadoutItemKey.fromValue(key)?.let { it to enabled } }
                .toMap()
        }

    override suspend fun saveEnabledState(key: ReadoutItemKey, enabled: Boolean) {
        dataStore.updateData { it.copy(enabledStates = it.enabledStates + (key.value to enabled)) }
    }
}
