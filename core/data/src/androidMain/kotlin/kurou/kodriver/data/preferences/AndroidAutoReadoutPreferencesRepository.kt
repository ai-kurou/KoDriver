package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.AutoReadoutPreferencesRepository

internal class AndroidAutoReadoutPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) : AutoReadoutPreferencesRepository {
    override suspend fun resetToDefault() {
        dataStore.edit { it.clear() }
    }

    private fun enabledKey(
        simulator: String,
        key: ReadoutItemKey,
    ) = booleanPreferencesKey("${simulator}_${key.value}_enabled")

    private fun orderKey(simulator: String) = stringPreferencesKey("${simulator}_order")

    override fun observeAutoReadoutEnabledStates(simulator: String): Flow<Map<ReadoutItemKey, Boolean>> =
        dataStore.data.fallbackOnReadError(emptyPreferences()).map { prefs ->
            val prefix = "${simulator}_"
            val suffix = "_enabled"
            prefs
                .asMap()
                .filterKeys { it.name.startsWith(prefix) && it.name.endsWith(suffix) }
                .mapKeys { (k, _) -> k.name.removePrefix(prefix).removeSuffix(suffix) }
                .mapValues { (_, v) -> v as Boolean }
                .mapNotNull { (key, enabled) -> ReadoutItemKey.fromValue(key)?.let { it to enabled } }
                .toMap()
        }

    override suspend fun saveAutoReadoutEnabledState(
        simulator: String,
        key: ReadoutItemKey,
        enabled: Boolean,
    ) {
        dataStore.edit { it[enabledKey(simulator, key)] = enabled }
    }

    override fun observeAutoReadoutOrder(simulator: String): Flow<List<ReadoutItemKey>> =
        dataStore.data.fallbackOnReadError(emptyPreferences()).map { prefs ->
            prefs[orderKey(simulator)]
                ?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.mapNotNull(ReadoutItemKey::fromValue)
                ?: emptyList()
        }

    override suspend fun saveAutoReadoutOrder(
        simulator: String,
        order: List<ReadoutItemKey>,
    ) {
        dataStore.edit { it[orderKey(simulator)] = order.joinToString(",") { key -> key.value } }
    }
}
