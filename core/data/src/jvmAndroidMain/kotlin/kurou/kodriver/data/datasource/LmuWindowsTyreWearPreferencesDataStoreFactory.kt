package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kurou.kodriver.data.model.LmuWindowsTyreWearPreferences
import java.io.File

internal fun createLmuWindowsTyreWearPreferencesDataStore(directory: String): DataStore<LmuWindowsTyreWearPreferences> =
    DataStoreFactory.create(
        serializer = LmuWindowsTyreWearPreferencesSerializer,
        produceFile = { File("$directory/lmu_windows_tyre_wear_preferences.pb") },
    )
