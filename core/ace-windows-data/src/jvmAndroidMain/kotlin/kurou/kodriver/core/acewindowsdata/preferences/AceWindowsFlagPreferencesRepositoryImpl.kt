package kurou.kodriver.core.acewindowsdata.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagPreferencesRepository
import kurou.kodriver.domain.model.ReadoutItemKey

internal class AceWindowsFlagPreferencesRepositoryImpl(
    private val dataStore: DataStore<AceWindowsFlagPreferences>,
) : AceWindowsFlagPreferencesRepository {
    override suspend fun resetToDefault() {
        dataStore.updateData { AceWindowsFlagPreferences() }
    }

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
