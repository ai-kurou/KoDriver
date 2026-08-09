package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import kurou.kodriver.data.model.LmuWindowsFlagPreferences

internal fun createLmuWindowsFlagPreferencesDataStore(directory: String): DataStore<LmuWindowsFlagPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_flag_preferences.pb",
        serializer = LmuWindowsFlagPreferencesSerializer,
    )
