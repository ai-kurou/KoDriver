package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kurou.kodriver.data.model.FlagPreferences
import java.io.File

internal fun createFlagPreferencesDataStore(directory: String): DataStore<FlagPreferences> =
    DataStoreFactory.create(
        serializer = FlagPreferencesSerializer,
        produceFile = { File("$directory/flag_preferences.pb") },
    )
