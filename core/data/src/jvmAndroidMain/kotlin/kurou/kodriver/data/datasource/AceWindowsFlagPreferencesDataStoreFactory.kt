package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import kurou.kodriver.data.model.AceWindowsFlagPreferences

internal fun createAceWindowsFlagPreferencesDataStore(directory: String): DataStore<AceWindowsFlagPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "ace_windows_flag_preferences.pb",
        serializer = AceWindowsFlagPreferencesSerializer,
    )
