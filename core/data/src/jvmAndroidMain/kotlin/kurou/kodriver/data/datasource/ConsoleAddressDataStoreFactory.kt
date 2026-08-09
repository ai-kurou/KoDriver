package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import kurou.kodriver.data.model.ConsoleAddressPreferences

internal fun createConsoleAddressDataStore(directory: String): DataStore<ConsoleAddressPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "console_address.pb",
        serializer = ConsoleAddressSerializer,
    )
