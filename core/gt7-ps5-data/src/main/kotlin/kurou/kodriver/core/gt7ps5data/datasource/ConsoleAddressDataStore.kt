package kurou.kodriver.core.gt7ps5data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import java.io.File

internal fun createConsoleAddressDataStore(directory: String): DataStore<ConsoleAddressPreferences> =
    DataStoreFactory.create(
        serializer = ConsoleAddressSerializer,
        produceFile = { File("$directory/console_address.pb") },
    )
