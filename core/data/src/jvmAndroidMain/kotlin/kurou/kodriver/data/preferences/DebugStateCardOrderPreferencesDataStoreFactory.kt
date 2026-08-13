package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createDebugStateCardOrderPreferencesDataStore(
    directory: String,
): DataStore<DebugStateCardOrderPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "debug_state_card_order_preferences.pb",
        serializer = DebugStateCardOrderPreferencesSerializer,
    )
