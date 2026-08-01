package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface ConsoleAddressPreferencesRepository {
    fun consoleAddress(): Flow<String?>

    suspend fun saveConsoleAddress(address: String)
}
