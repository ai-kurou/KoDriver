package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import kurou.kodriver.data.model.QueuePreferences

internal fun createQueuePreferencesDataStore(directory: String): DataStore<QueuePreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "queue_preferences.pb",
        serializer = QueuePreferencesSerializer,
    )
