package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kurou.kodriver.data.model.ReadoutPreferences
import java.io.File

fun createReadoutPreferencesDataStore(directory: String): DataStore<ReadoutPreferences> =
    DataStoreFactory.create(
        serializer = ReadoutPreferencesSerializer,
        produceFile = { File("$directory/readout_preferences.pb") },
    )
