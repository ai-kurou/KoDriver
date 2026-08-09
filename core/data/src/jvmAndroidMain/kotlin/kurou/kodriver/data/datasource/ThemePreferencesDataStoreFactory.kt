package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import kurou.kodriver.data.model.ThemePreferences

internal fun createThemePreferencesDataStore(directory: String): DataStore<ThemePreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "theme_preferences.pb",
        serializer = ThemePreferencesSerializer,
    )
