package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.VOICE_ID_UNSPECIFIED
import kurou.kodriver.domain.repository.VoicePreferencesRepository
import kotlin.test.Test

class SaveVoiceUseCaseTest {
    private val repository: VoicePreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `音声IDと空文字を保存できる`() =
        runTest {
            val useCase = SaveVoiceUseCase(repository)

            useCase("Microsoft Haruka Desktop")
            useCase("ja-jp-x-jab-local")
            useCase(VOICE_ID_UNSPECIFIED)

            coVerify(exactly = 1) { repository.saveVoiceId("Microsoft Haruka Desktop") }
            coVerify(exactly = 1) { repository.saveVoiceId("ja-jp-x-jab-local") }
            coVerify(exactly = 1) { repository.saveVoiceId(VOICE_ID_UNSPECIFIED) }
            confirmVerified(repository)
        }
}
