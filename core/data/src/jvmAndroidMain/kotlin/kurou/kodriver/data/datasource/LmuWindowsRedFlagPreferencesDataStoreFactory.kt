package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kurou.kodriver.data.model.RedFlagPreferences
import java.io.File

internal fun createLmuWindowsRedFlagPreferencesDataStore(directory: String): DataStore<RedFlagPreferences> =
    DataStoreFactory.create(
        serializer = RedFlagPreferencesSerializer,
        produceFile = { File("$directory/lmu_windows_red_flag_preferences.pb") },
    )
