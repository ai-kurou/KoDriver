package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kurou.kodriver.data.model.MyBestLapPreferences
import java.io.File

internal fun createGt7Ps5MyBestLapPreferencesDataStore(directory: String): DataStore<MyBestLapPreferences> =
    DataStoreFactory.create(
        serializer = MyBestLapPreferencesSerializer,
        produceFile = { File("$directory/my_best_lap_preferences.pb") },
    )
