package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kurou.kodriver.data.model.LmuWindowsFlagPreferences
import java.io.File

internal fun createLmuWindowsFlagPreferencesDataStore(directory: String): DataStore<LmuWindowsFlagPreferences> =
    DataStoreFactory.create(
        serializer = LmuWindowsFlagPreferencesSerializer,
        produceFile = { File("$directory/lmu_windows_flag_preferences.pb") },
    )
