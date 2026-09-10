package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createLmuWindowsRainPreferencesDataStore(directory: String): DataStore<LmuWindowsRainPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_rain_preferences.pb",
        serializer = LmuWindowsRainPreferencesSerializer,
    )
