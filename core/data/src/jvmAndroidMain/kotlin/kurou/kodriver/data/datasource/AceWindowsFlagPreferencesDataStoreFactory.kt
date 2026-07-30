package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kurou.kodriver.data.model.AceWindowsFlagPreferences
import java.io.File

internal fun createAceWindowsFlagPreferencesDataStore(directory: String): DataStore<AceWindowsFlagPreferences> =
    DataStoreFactory.create(
        serializer = AceWindowsFlagPreferencesSerializer,
        produceFile = { File("$directory/ace_windows_flag_preferences.pb") },
    )
