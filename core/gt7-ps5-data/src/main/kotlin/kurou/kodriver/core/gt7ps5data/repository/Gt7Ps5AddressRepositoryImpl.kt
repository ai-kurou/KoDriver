package kurou.kodriver.core.gt7ps5data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.core.gt7ps5data.datasource.Gt7Ps5AddressPreferences
import kurou.kodriver.domain.repository.Gt7Ps5AddressRepository

internal class Gt7Ps5AddressRepositoryImpl(
    private val dataStore: DataStore<Gt7Ps5AddressPreferences>,
) : Gt7Ps5AddressRepository {
    override fun gt7Ps5Address(): Flow<String?> =
        dataStore.data.map { it.address.ifEmpty { null } }

    override suspend fun saveGt7Ps5Address(address: String) {
        dataStore.updateData { it.copy(address = address) }
    }
}
