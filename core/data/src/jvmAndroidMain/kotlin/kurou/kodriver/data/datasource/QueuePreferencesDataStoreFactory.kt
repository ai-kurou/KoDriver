package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kurou.kodriver.data.model.QueuePreferences
import java.io.File

internal fun createQueuePreferencesDataStore(directory: String): DataStore<QueuePreferences> =
    DataStoreFactory.create(
        serializer = QueuePreferencesSerializer,
        produceFile = { File("$directory/queue_preferences.pb") },
    )
