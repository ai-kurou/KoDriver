package kurou.kodriver.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository

internal class AndroidReadoutPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) : ReadoutPreferencesRepository {

    private fun enabledKey(simulator: String, key: ReadoutItemKey) =
        booleanPreferencesKey("${simulator}_${key.value}_enabled")

    private fun orderKey(simulator: String) =
        stringPreferencesKey("${simulator}_order")

    override fun observeReadoutEnabledStates(simulator: String): Flow<Map<ReadoutItemKey, Boolean>> =
        dataStore.data.map { prefs ->
            val prefix = "${simulator}_"
            val suffix = "_enabled"
            @Suppress("UNCHECKED_CAST")
            prefs.asMap()
                .filterKeys { it.name.startsWith(prefix) && it.name.endsWith(suffix) }
                .mapKeys { (k, _) -> k.name.removePrefix(prefix).removeSuffix(suffix) }
                .mapValues { (_, v) -> v as Boolean }
                .mapNotNull { (key, enabled) -> ReadoutItemKey.fromValue(key)?.let { it to enabled } }
                .toMap()
        }

    override suspend fun saveReadoutEnabledState(simulator: String, key: ReadoutItemKey, enabled: Boolean) {
        dataStore.edit { it[enabledKey(simulator, key)] = enabled }
    }

    override fun observeReadoutOrder(simulator: String): Flow<List<ReadoutItemKey>> =
        dataStore.data.map { prefs ->
            prefs[orderKey(simulator)]
                ?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.mapNotNull(ReadoutItemKey::fromValue)
                ?: emptyList()
        }

    override suspend fun saveReadoutOrder(simulator: String, order: List<ReadoutItemKey>) {
        dataStore.edit { it[orderKey(simulator)] = order.joinToString(",") { key -> key.value } }
    }
}
