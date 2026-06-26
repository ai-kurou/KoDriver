package kurou.kodriver.core.gt7ps5data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kurou.kodriver.core.gt7ps5data.model.Gt7UdpPortPreferences
import java.io.File

internal fun createGt7UdpPortPreferencesDataStore(directory: String): DataStore<Gt7UdpPortPreferences> =
    DataStoreFactory.create(
        serializer = Gt7UdpPortPreferencesSerializer,
        produceFile = { File("$directory/gt7_udp_port_preferences.pb") },
    )
