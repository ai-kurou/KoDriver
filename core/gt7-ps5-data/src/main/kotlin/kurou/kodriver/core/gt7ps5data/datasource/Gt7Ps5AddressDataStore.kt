package kurou.kodriver.core.gt7ps5data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import java.io.File

internal fun createGt7Ps5AddressDataStore(directory: String): DataStore<Gt7Ps5AddressPreferences> =
    DataStoreFactory.create(
        serializer = Gt7Ps5AddressSerializer,
        produceFile = { File("$directory/gt7_ps5_address.pb") },
    )
