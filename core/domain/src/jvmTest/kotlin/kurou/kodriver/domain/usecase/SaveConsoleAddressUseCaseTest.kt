package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.ConsoleAddressPreferencesRepository
import kotlin.test.Test

class SaveConsoleAddressUseCaseTest {

    @Test
    fun `アドレスを保存する`() = runBlocking {
        val repository = mockk<ConsoleAddressPreferencesRepository>(relaxUnitFun = true)

        SaveConsoleAddressUseCase(repository)("192.168.1.50")

        coVerify(exactly = 1) { repository.saveConsoleAddress("192.168.1.50") }
        confirmVerified(repository)
    }
}
