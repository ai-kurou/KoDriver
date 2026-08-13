package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createConsoleAddressDataStore(directory: String): DataStore<ConsoleAddressPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "console_address.pb",
        serializer = ConsoleAddressSerializer,
    )
