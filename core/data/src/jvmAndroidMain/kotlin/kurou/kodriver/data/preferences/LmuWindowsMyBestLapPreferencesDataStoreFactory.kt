package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createLmuWindowsMyBestLapPreferencesDataStore(directory: String): DataStore<MyBestLapPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_my_best_lap_preferences.pb",
        serializer = MyBestLapPreferencesSerializer,
    )
