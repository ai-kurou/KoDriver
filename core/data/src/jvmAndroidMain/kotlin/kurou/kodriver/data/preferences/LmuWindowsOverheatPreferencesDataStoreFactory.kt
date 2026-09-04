package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createLmuWindowsOverheatPreferencesDataStore(directory: String): DataStore<OverheatPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_overheat_preferences.pb",
        serializer = OverheatPreferencesSerializer,
    )
