package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createSimulatorPreferencesDataStore(directory: String): DataStore<SimulatorPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "simulator_preferences.pb",
        serializer = SimulatorPreferencesSerializer,
    )
