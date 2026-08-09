package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import kurou.kodriver.data.model.DebugStateCardOrderPreferences

internal fun createDebugStateCardOrderPreferencesDataStore(
    directory: String,
): DataStore<DebugStateCardOrderPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "debug_state_card_order_preferences.pb",
        serializer = DebugStateCardOrderPreferencesSerializer,
    )
