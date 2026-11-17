package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

public interface ConsoleAddressPreferencesRepository {
    public fun consoleAddress(): Flow<String?>

    public suspend fun saveConsoleAddress(address: String)
}
