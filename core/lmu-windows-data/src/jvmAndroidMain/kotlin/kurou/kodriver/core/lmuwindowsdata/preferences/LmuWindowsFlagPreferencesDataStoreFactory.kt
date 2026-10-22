package kurou.kodriver.core.lmuwindowsdata.preferences

import androidx.datastore.core.DataStore

internal fun createLmuWindowsFlagPreferencesDataStore(directory: String): DataStore<LmuWindowsFlagPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_auto_flag_preferences.pb",
        serializer = LmuWindowsFlagPreferencesSerializer,
    )
