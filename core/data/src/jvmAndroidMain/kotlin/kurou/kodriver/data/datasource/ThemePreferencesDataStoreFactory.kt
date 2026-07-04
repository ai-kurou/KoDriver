package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kurou.kodriver.data.model.ThemePreferences
import java.io.File

internal fun createThemePreferencesDataStore(directory: String): DataStore<ThemePreferences> =
    DataStoreFactory.create(
        serializer = ThemePreferencesSerializer,
        produceFile = { File("$directory/theme_preferences.pb") },
    )
