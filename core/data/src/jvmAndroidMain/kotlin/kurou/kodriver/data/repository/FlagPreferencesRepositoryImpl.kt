package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.data.model.FlagPreferences
import kurou.kodriver.domain.repository.FlagPreferencesRepository

internal class FlagPreferencesRepositoryImpl(
    private val dataStore: DataStore<FlagPreferences>,
) : FlagPreferencesRepository {

    override fun observeFlagEnabledStates(): Flow<Map<String, Boolean>> =
        dataStore.data.map { it.enabledStates }

    override suspend fun saveFlagEnabledState(key: String, enabled: Boolean) {
        dataStore.updateData { it.copy(enabledStates = it.enabledStates + (key to enabled)) }
    }
}
