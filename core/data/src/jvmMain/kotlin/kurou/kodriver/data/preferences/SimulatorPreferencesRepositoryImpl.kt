package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.SELECTED_SIMULATOR_DEFAULT
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository

internal class SimulatorPreferencesRepositoryImpl(
    private val dataStore: DataStore<SimulatorPreferences>,
) : SimulatorPreferencesRepository {
    override fun selectedSimulator(): Flow<Simulator> =
        dataStore.observeProperty {
            Simulator.fromId(it.selectedSimulator) ?: SELECTED_SIMULATOR_DEFAULT
        }

    override suspend fun saveSelectedSimulator(simulator: Simulator) {
        dataStore.saveProperty(simulator.id) { prefs, value -> prefs.copy(selectedSimulator = value) }
    }
}
