package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.data.model.ConsoleAddressPreferences
import kurou.kodriver.domain.repository.ConsoleAddressPreferencesRepository

internal class ConsoleAddressPreferencesRepositoryImpl(
    private val dataStore: DataStore<ConsoleAddressPreferences>,
) : ConsoleAddressPreferencesRepository {
    override fun consoleAddress(): Flow<String?> = dataStore.observeProperty { it.address.ifEmpty { null } }

    override suspend fun saveConsoleAddress(address: String) {
        dataStore.saveProperty(address) { prefs, value -> prefs.copy(address = value) }
    }
}
