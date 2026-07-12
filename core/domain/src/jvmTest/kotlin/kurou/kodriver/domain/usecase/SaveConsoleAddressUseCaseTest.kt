package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.ConsoleAddressPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createConsoleAddressPreferencesRepository(
    initial: String? = null,
): ConsoleAddressPreferencesRepository {
    val repository = mockk<ConsoleAddressPreferencesRepository>()
    val state = MutableStateFlow(initial)
    every { repository.consoleAddress() } returns state
    coEvery { repository.saveConsoleAddress(any()) } answers {
        state.update { firstArg() }
    }
    return repository
}

class SaveConsoleAddressUseCaseTest {

    @Test
    fun `アドレスを保存する`() = runBlocking {
        val repo = createConsoleAddressPreferencesRepository()
        SaveConsoleAddressUseCase(repo)("192.168.1.50")
        assertEquals("192.168.1.50", repo.consoleAddress().first())
    }
}
