package kurou.kodriver.core.lmuwindowsdata.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsFlagPreferencesRepository
import kurou.kodriver.domain.model.ReadoutItemKey

internal class LmuWindowsFlagPreferencesRepositoryImpl(
    private val dataStore: DataStore<LmuWindowsFlagPreferences>,
) : LmuWindowsFlagPreferencesRepository {
    override fun observeFlagEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>> =
        dataStore.observeProperty { prefs ->
            prefs.enabledStates
                .mapNotNull { (key, enabled) -> ReadoutItemKey.fromValue(key)?.let { it to enabled } }
                .toMap()
        }

    override suspend fun saveFlagEnabledState(
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
