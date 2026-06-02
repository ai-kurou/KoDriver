package kurou.kodriver.data.datasource

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import okio.Path.Companion.toPath

fun createSimulatorPreferencesDataStore(directory: String) =
    PreferenceDataStoreFactory.createWithPath {
        "$directory/simulator_preferences.preferences_pb".toPath()
    }
