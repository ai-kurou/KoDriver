package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.data.model.ReadoutPreferences
import kurou.kodriver.data.model.SimulatorReadoutState
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository

internal class ReadoutPreferencesRepositoryImpl(
    private val dataStore: DataStore<ReadoutPreferences>,
) : ReadoutPreferencesRepository {

    override fun observeReadoutEnabledStates(simulator: String): Flow<Map<String, Boolean>> =
        dataStore.data.map { prefs ->
            prefs.simulatorStates[simulator]?.enabledStates ?: emptyMap()
        }

    override suspend fun saveReadoutEnabledState(simulator: String, label: String, enabled: Boolean) {
        dataStore.updateData { prefs ->
            val current = prefs.simulatorStates[simulator]?.enabledStates ?: emptyMap()
            val newState = SimulatorReadoutState(enabledStates = current + (label to enabled))
            prefs.copy(simulatorStates = prefs.simulatorStates + (simulator to newState))
        }
    }
}
