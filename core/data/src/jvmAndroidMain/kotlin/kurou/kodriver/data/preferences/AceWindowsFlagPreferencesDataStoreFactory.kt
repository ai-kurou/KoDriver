package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createAceWindowsFlagPreferencesDataStore(directory: String): DataStore<AceWindowsFlagPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "ace_windows_flag_preferences.pb",
        serializer = AceWindowsFlagPreferencesSerializer,
    )
