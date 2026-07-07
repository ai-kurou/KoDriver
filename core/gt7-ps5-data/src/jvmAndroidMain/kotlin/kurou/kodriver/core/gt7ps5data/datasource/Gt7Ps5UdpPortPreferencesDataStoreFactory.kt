package kurou.kodriver.core.gt7ps5data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kurou.kodriver.core.gt7ps5data.model.Gt7Ps5UdpPortPreferences
import java.io.File

internal fun createGt7Ps5UdpPortPreferencesDataStore(directory: String): DataStore<Gt7Ps5UdpPortPreferences> =
    DataStoreFactory.create(
        serializer = Gt7Ps5UdpPortPreferencesSerializer,
        produceFile = { File("$directory/gt7_udp_port_preferences.pb") },
    )
