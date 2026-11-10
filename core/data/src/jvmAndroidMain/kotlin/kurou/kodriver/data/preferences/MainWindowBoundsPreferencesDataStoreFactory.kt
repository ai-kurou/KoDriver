package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createMainWindowBoundsPreferencesDataStore(directory: String): DataStore<MainWindowBoundsPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "main_window_bounds_preferences.pb",
        serializer = MainWindowBoundsPreferencesSerializer,
    )
