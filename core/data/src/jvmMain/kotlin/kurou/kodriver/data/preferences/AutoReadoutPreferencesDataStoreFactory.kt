package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createAutoReadoutPreferencesDataStore(directory: String): DataStore<AutoReadoutPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "auto_readout_preferences.pb",
        serializer = AutoReadoutPreferencesSerializer,
    )
