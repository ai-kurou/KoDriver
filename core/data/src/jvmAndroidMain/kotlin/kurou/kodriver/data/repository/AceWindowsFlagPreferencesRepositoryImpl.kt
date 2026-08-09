package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.data.model.AceWindowsFlagPreferences
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.AceWindowsFlagPreferencesRepository

internal class AceWindowsFlagPreferencesRepositoryImpl(
    private val dataStore: DataStore<AceWindowsFlagPreferences>,
) : AceWindowsFlagPreferencesRepository {
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
