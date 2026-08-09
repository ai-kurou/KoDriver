package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import kurou.kodriver.data.model.RedFlagPreferences

internal fun createLmuWindowsRedFlagPreferencesDataStore(directory: String): DataStore<RedFlagPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_red_flag_preferences.pb",
        serializer = RedFlagPreferencesSerializer,
    )
