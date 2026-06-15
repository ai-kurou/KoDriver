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
            prefs.simulatorStates.getOrElse(simulator) { SimulatorReadoutState() }.enabledStates
        }

    override suspend fun saveReadoutEnabledState(simulator: String, label: String, enabled: Boolean) {
        dataStore.updateData { prefs ->
            val current = prefs.simulatorStates[simulator] ?: SimulatorReadoutState()
            val newState = current.copy(enabledStates = current.enabledStates + (label to enabled))
            prefs.copy(simulatorStates = prefs.simulatorStates + (simulator to newState))
        }
    }

    override fun observeReadoutOrder(simulator: String): Flow<List<String>> =
        dataStore.data.map { prefs ->
            prefs.simulatorStates.getOrElse(simulator) { SimulatorReadoutState() }.itemOrder
        }

    override suspend fun saveReadoutOrder(simulator: String, order: List<String>) {
        dataStore.updateData { prefs ->
            val current = prefs.simulatorStates[simulator] ?: SimulatorReadoutState()
            val newState = current.copy(itemOrder = order)
            prefs.copy(simulatorStates = prefs.simulatorStates + (simulator to newState))
        }
    }
}
