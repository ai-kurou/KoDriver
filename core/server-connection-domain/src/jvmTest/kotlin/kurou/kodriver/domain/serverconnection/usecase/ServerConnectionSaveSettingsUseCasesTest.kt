package kurou.kodriver.domain.serverconnection.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.serverconnection.repository.ConsoleAddressPreferencesRepository
import kotlin.test.Test

class ServerConnectionSaveSettingsUseCasesTest {
    @Test
    fun `SaveConsoleAddressUseCaseはRepositoryの対応するメソッドへ委譲する`() =
        runTest {
            assertSaves<ConsoleAddressPreferencesRepository>(
                useCaseName = "SaveConsoleAddressUseCase",
                save = { saveConsoleAddress("192.168.1.50") },
                useCase = { SaveConsoleAddressUseCase(it)("192.168.1.50") },
            )
        }

    private suspend inline fun <reified R : Any> assertSaves(
        useCaseName: String,
        crossinline save: suspend R.() -> Unit,
        crossinline useCase: suspend (R) -> Unit,
    ) {
        val repository = mockk<R>(relaxUnitFun = true)
        try {
            useCase(repository)

            coVerify(exactly = 1) { repository.save() }
            confirmVerified(repository)
        } catch (e: AssertionError) {
            throw AssertionError("$useCaseName: ${e.message}", e)
        }
    }
}
