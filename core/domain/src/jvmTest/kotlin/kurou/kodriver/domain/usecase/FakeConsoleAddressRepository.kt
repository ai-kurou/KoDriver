package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kurou.kodriver.domain.repository.ConsoleAddressRepository

internal class FakeConsoleAddressRepository(initial: String? = null) : ConsoleAddressRepository {
    private val flow = MutableStateFlow(initial)
    override fun consoleAddress(): Flow<String?> = flow
    override suspend fun saveConsoleAddress(address: String) { flow.value = address }
}
