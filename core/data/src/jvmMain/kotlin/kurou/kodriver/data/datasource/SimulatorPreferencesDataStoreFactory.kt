package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import kurou.kodriver.data.model.SimulatorPreferences

internal fun createSimulatorPreferencesDataStore(directory: String): DataStore<SimulatorPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "simulator_preferences.pb",
        serializer = SimulatorPreferencesSerializer,
    )
