package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.VoicePreferencesRepository
import kotlin.test.Test

class VoiceSaveSettingsUseCasesTest {
    @Test
    fun `設定保存UseCaseはRepositoryの対応するメソッドへ委譲する`() =
        runTest {
            assertSaves<VoicePreferencesRepository>(
                useCaseName = "SaveVoiceUseCase",
                save = { saveVoiceId("ja-jp-x-jab-local") },
                useCase = { SaveVoiceUseCase(it)("ja-jp-x-jab-local") },
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
