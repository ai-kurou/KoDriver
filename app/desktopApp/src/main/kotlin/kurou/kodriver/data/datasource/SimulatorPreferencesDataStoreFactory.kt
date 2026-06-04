package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kurou.kodriver.data.model.SimulatorPreferences
import java.io.File

internal fun createSimulatorPreferencesDataStore(directory: String): DataStore<SimulatorPreferences> =
    DataStoreFactory.create(
        serializer = SimulatorPreferencesSerializer,
        produceFile = { File("$directory/simulator_preferences.pb") },
    )
