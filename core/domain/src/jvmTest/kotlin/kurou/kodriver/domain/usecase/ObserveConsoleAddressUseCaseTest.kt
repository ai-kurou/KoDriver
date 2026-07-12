package kurou.kodriver.domain.usecase

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.ConsoleAddressPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ObserveConsoleAddressUseCaseTest {

    @Test
    fun `保存済みアドレスを返す`() = runBlocking {
        val repo = mockk<ConsoleAddressPreferencesRepository>()
        every { repo.consoleAddress() } returns MutableStateFlow("192.168.1.100")
        assertEquals("192.168.1.100", ObserveConsoleAddressUseCase(repo)().first())
    }

    @Test
    fun `未設定の場合はnullを返す`() = runBlocking {
        val repo = mockk<ConsoleAddressPreferencesRepository>()
        every { repo.consoleAddress() } returns MutableStateFlow(null)
        assertNull(ObserveConsoleAddressUseCase(repo)().first())
    }
}
