package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createLmuWindowsRedFlagPreferencesDataStore(directory: String): DataStore<RedFlagPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_red_flag_preferences.pb",
        serializer = RedFlagPreferencesSerializer,
    )
