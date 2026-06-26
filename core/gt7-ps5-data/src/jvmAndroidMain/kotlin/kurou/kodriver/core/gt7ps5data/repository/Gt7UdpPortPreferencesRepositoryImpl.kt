package kurou.kodriver.core.gt7ps5data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.core.gt7ps5data.model.Gt7UdpPortPreferences
import kurou.kodriver.domain.repository.Gt7UdpPortPreferencesRepository

internal class Gt7UdpPortPreferencesRepositoryImpl(
    private val dataStore: DataStore<Gt7UdpPortPreferences>,
) : Gt7UdpPortPreferencesRepository {

    override fun port(): Flow<Int> = dataStore.data.map { it.port }

    override suspend fun savePort(port: Int) {
        dataStore.updateData { it.copy(port = port) }
    }
}
