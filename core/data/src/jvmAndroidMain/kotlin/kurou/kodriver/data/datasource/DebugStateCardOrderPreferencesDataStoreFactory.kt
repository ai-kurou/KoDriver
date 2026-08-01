package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kurou.kodriver.data.model.DebugStateCardOrderPreferences
import java.io.File

internal fun createDebugStateCardOrderPreferencesDataStore(
    directory: String,
): DataStore<DebugStateCardOrderPreferences> =
    DataStoreFactory.create(
        serializer = DebugStateCardOrderPreferencesSerializer,
        produceFile = { File("$directory/debug_state_card_order_preferences.pb") },
    )
