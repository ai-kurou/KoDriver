package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createReadoutPreferencesDataStore(directory: String): DataStore<ReadoutPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "auto_readout_preferences.pb",
        serializer = ReadoutPreferencesSerializer,
    )
