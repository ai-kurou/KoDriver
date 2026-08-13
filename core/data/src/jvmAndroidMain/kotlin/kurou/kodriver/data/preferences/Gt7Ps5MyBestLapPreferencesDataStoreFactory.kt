package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createGt7Ps5MyBestLapPreferencesDataStore(directory: String): DataStore<MyBestLapPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "gt7_ps5_my_best_lap_preferences.pb",
        serializer = MyBestLapPreferencesSerializer,
    )
