package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository

internal class ReadoutPreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : ReadoutPreferencesRepository {

    override fun observeReadoutEnabledStates(simulator: String): Flow<Map<String, Boolean>> =
        dataStore.data.map { prefs ->
            prefs[readoutEnabledStatesKey(simulator)]?.deserialize() ?: emptyMap()
        }

    override suspend fun saveReadoutEnabledState(simulator: String, label: String, enabled: Boolean) {
        val key = readoutEnabledStatesKey(simulator)
        dataStore.edit { prefs ->
            val current = prefs[key]?.deserialize() ?: emptyMap()
            prefs[key] = (current + (label to enabled)).serialize()
        }
    }

    private fun readoutEnabledStatesKey(simulator: String) =
        stringPreferencesKey("readout_enabled_states_$simulator")

    private fun Map<String, Boolean>.serialize(): String =
        entries.joinToString("\n") { "${it.key}=${it.value}" }

    private fun String.deserialize(): Map<String, Boolean> =
        lines()
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val lastEquals = line.lastIndexOf('=')
                if (lastEquals < 0) return@mapNotNull null
                val label = line.substring(0, lastEquals)
                val value = line.substring(lastEquals + 1).toBoolean()
                label to value
            }
            .toMap()
}
