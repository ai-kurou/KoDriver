package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.data.model.SimulatorPreferences
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository

internal class SimulatorPreferencesRepositoryImpl(
    private val dataStore: DataStore<SimulatorPreferences>,
) : SimulatorPreferencesRepository {

    override fun selectedSimulator(): Flow<Simulator?> =
        dataStore.data.map { Simulator.fromId(it.selectedSimulator) }

    override suspend fun saveSelectedSimulator(simulator: Simulator) {
        dataStore.updateData { it.copy(selectedSimulator = simulator.id) }
    }
}
