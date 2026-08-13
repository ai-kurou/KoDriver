package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createLmuWindowsFlagPreferencesDataStore(directory: String): DataStore<LmuWindowsFlagPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_flag_preferences.pb",
        serializer = LmuWindowsFlagPreferencesSerializer,
    )
