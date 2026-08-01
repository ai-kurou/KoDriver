package kurou.kodriver.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository

internal class AndroidSimulatorPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) : SimulatorPreferencesRepository {
    private val selectedSimulatorKey = stringPreferencesKey("selected_simulator")

    override fun selectedSimulator(): Flow<Simulator?> =
        dataStore.data.map {
            Simulator.fromId(it[selectedSimulatorKey].orEmpty())
        }

    override suspend fun saveSelectedSimulator(simulator: Simulator) {
        dataStore.edit { it[selectedSimulatorKey] = simulator.id }
    }
}
