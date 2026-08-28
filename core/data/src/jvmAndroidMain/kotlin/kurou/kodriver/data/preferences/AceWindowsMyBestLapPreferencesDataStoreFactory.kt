package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createAceWindowsMyBestLapPreferencesDataStore(directory: String): DataStore<MyBestLapPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "ace_windows_my_best_lap_preferences.pb",
        serializer = MyBestLapPreferencesSerializer,
    )
