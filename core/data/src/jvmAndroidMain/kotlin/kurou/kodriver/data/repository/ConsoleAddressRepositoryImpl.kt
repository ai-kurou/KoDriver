package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.data.model.ConsoleAddressPreferences
import kurou.kodriver.domain.repository.ConsoleAddressRepository

internal class ConsoleAddressRepositoryImpl(
    private val dataStore: DataStore<ConsoleAddressPreferences>,
) : ConsoleAddressRepository {
    override fun consoleAddress(): Flow<String?> =
        dataStore.data.map { it.address.ifEmpty { null } }

    override suspend fun saveConsoleAddress(address: String) {
        dataStore.updateData { it.copy(address = address) }
    }
}
