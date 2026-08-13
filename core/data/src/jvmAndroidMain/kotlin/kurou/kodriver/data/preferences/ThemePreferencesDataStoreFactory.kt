package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createThemePreferencesDataStore(directory: String): DataStore<ThemePreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "theme_preferences.pb",
        serializer = ThemePreferencesSerializer,
    )
