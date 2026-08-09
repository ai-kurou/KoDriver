package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.data.model.QueuePreferences
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.QueuePreferencesRepository

internal class QueuePreferencesRepositoryImpl(
    private val dataStore: DataStore<QueuePreferences>,
) : QueuePreferencesRepository {
    override fun observeQueueEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>> =
        dataStore.observeProperty { prefs ->
            prefs.enabledStates
                .mapNotNull { (key, enabled) -> ReadoutItemKey.fromValue(key)?.let { it to enabled } }
                .toMap()
        }

    override suspend fun saveQueueEnabledState(
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
