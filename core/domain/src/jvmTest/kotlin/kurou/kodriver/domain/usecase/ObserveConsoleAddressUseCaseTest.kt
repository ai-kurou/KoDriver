package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.ConsoleAddressPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ObserveConsoleAddressUseCaseTest {
    @MockK
    private lateinit var repo: ConsoleAddressPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `保存済みアドレスを返す`() =
        runBlocking {
            every { repo.consoleAddress() } returns MutableStateFlow("192.168.1.100")
            assertEquals("192.168.1.100", ObserveConsoleAddressUseCase(repo)().first())
            verify(exactly = 1) { repo.consoleAddress() }
            confirmVerified(repo)
        }

    @Test
    fun `未設定の場合はnullを返す`() =
        runBlocking {
            every { repo.consoleAddress() } returns MutableStateFlow(null)
            assertNull(ObserveConsoleAddressUseCase(repo)().first())
            verify(exactly = 1) { repo.consoleAddress() }
            confirmVerified(repo)
        }
}
