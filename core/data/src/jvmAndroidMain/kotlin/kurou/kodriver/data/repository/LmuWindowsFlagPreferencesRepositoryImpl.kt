package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.data.model.LmuWindowsFlagPreferences
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsFlagPreferencesRepository

internal class LmuWindowsFlagPreferencesRepositoryImpl(
    private val dataStore: DataStore<LmuWindowsFlagPreferences>,
) : LmuWindowsFlagPreferencesRepository {
    override fun observeFlagEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>> =
        dataStore.data.map { prefs ->
            prefs.enabledStates
                .mapNotNull { (key, enabled) -> ReadoutItemKey.fromValue(key)?.let { it to enabled } }
                .toMap()
        }

    override suspend fun saveFlagEnabledState(key: ReadoutItemKey, enabled: Boolean) {
        dataStore.updateData { it.copy(enabledStates = it.enabledStates + (key.value to enabled)) }
    }
}
