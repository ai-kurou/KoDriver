package kurou.kodriver.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.repository.ServerIpPreferencesRepository

internal class AndroidServerIpPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) : ServerIpPreferencesRepository {
    private val serverIpKey = stringPreferencesKey("server_ip")

    override fun serverIp(): Flow<String?> = dataStore.data.map { it[serverIpKey] }

    override suspend fun saveServerIp(ip: String) {
        dataStore.edit { it[serverIpKey] = ip }
    }
}
