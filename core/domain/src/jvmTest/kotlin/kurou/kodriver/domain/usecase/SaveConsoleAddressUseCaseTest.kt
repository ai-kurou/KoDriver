package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.ConsoleAddressPreferencesRepository
import kotlin.test.Test

class SaveConsoleAddressUseCaseTest {
    private val repository: ConsoleAddressPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `アドレスを保存する`() =
        runTest {
            SaveConsoleAddressUseCase(repository)("192.168.1.50")

            coVerify(exactly = 1) { repository.saveConsoleAddress("192.168.1.50") }
            confirmVerified(repository)
        }
}
