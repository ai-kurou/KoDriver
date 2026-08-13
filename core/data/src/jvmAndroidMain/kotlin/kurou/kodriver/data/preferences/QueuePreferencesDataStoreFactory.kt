package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createQueuePreferencesDataStore(directory: String): DataStore<QueuePreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "queue_preferences.pb",
        serializer = QueuePreferencesSerializer,
    )
