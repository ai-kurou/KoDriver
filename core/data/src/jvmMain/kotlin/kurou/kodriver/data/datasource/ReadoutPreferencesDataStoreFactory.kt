package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import kurou.kodriver.data.model.ReadoutPreferences

internal fun createReadoutPreferencesDataStore(directory: String): DataStore<ReadoutPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "readout_preferences.pb",
        serializer = ReadoutPreferencesSerializer,
    )
