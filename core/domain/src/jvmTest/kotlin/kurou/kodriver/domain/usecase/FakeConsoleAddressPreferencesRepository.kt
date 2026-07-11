package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kurou.kodriver.domain.repository.ConsoleAddressPreferencesRepository

internal class FakeConsoleAddressPreferencesRepository(initial: String? = null) : ConsoleAddressPreferencesRepository {
    private val flow = MutableStateFlow(initial)
    override fun consoleAddress(): Flow<String?> = flow
    override suspend fun saveConsoleAddress(address: String) { flow.value = address }
}
