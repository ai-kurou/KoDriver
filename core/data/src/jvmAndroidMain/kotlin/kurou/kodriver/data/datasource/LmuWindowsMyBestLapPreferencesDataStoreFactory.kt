package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import kurou.kodriver.data.model.MyBestLapPreferences

internal fun createLmuWindowsMyBestLapPreferencesDataStore(directory: String): DataStore<MyBestLapPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_my_best_lap_preferences.pb",
        serializer = MyBestLapPreferencesSerializer,
    )
