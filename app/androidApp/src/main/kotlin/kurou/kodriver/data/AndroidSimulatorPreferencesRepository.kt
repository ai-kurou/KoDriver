package kurou.kodriver.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository

internal class AndroidSimulatorPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) : SimulatorPreferencesRepository {

    private val keySelectedSimulator = stringPreferencesKey("selected_simulator")

    override fun selectedSimulator(): Flow<String?> =
        dataStore.data.map { it[keySelectedSimulator] }

    override suspend fun saveSelectedSimulator(simulator: String) {
        dataStore.edit { it[keySelectedSimulator] = simulator }
    }
}
