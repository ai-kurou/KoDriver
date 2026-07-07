package kurou.kodriver.core.gt7ps5data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.core.gt7ps5data.model.Gt7Ps5UdpPortPreferences
import kurou.kodriver.domain.repository.Gt7Ps5UdpPortPreferencesRepository

internal class Gt7Ps5UdpPortPreferencesRepositoryImpl(
    private val dataStore: DataStore<Gt7Ps5UdpPortPreferences>,
) : Gt7Ps5UdpPortPreferencesRepository {

    override fun port(): Flow<Int> = dataStore.data.map { it.port }

    override suspend fun savePort(port: Int) {
        dataStore.updateData { it.copy(port = port) }
    }
}
