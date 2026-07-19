package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.ConsoleAddressPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveConsoleAddressUseCaseTest {

    @MockK(relaxUnitFun = true)
    private lateinit var repository: ConsoleAddressPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `アドレスを保存する`() = runBlocking {
        SaveConsoleAddressUseCase(repository)("192.168.1.50")

        coVerify(exactly = 1) { repository.saveConsoleAddress("192.168.1.50") }
        confirmVerified(repository)
    }
}
