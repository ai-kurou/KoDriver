package kurou.kodriver.domain.manualreadout.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.manualreadout.repository.ManualReadoutPlayButtonLayoutEnabledPreferencesRepository
import kotlin.test.Test

class ManualReadoutSaveSettingsUseCasesTest {
    @Test
    fun `設定保存UseCaseはRepositoryの対応するメソッドへ委譲する`() =
        runTest {
            assertSaves<ManualReadoutPlayButtonLayoutEnabledPreferencesRepository>(
                useCaseName = "SaveManualReadoutPlayButtonLayoutEnabledUseCase",
                save = { saveManualReadoutPlayButtonLayoutEnabled(true) },
                useCase = { SaveManualReadoutPlayButtonLayoutEnabledUseCase(it)(true) },
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
