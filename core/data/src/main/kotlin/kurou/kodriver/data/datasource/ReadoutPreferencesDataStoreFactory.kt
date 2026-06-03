package kurou.kodriver.data.datasource

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import okio.Path.Companion.toPath

fun createReadoutPreferencesDataStore(directory: String) =
    PreferenceDataStoreFactory.createWithPath {
        "$directory/readout_preferences.preferences_pb".toPath()
    }
