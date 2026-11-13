package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.SPEECH_RECOGNITION_RESUME_DELAY_MS_MAX
import kurou.kodriver.domain.model.SPEECH_RECOGNITION_RESUME_DELAY_MS_MIN
import kurou.kodriver.domain.repository.SpeechRecognitionResumeDelayPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertFailsWith

class SaveSpeechRecognitionResumeDelayUseCaseTest {
    private val repository: SpeechRecognitionResumeDelayPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `範囲内の値を保存できる`() =
        runTest {
            val useCase = SaveSpeechRecognitionResumeDelayUseCase(repository)

            useCase(SPEECH_RECOGNITION_RESUME_DELAY_MS_MIN)
            useCase(1_000)
            useCase(SPEECH_RECOGNITION_RESUME_DELAY_MS_MAX)

            coVerify(exactly = 1) { repository.saveResumeDelayMs(SPEECH_RECOGNITION_RESUME_DELAY_MS_MIN) }
            coVerify(exactly = 1) { repository.saveResumeDelayMs(1_000) }
            coVerify(exactly = 1) { repository.saveResumeDelayMs(SPEECH_RECOGNITION_RESUME_DELAY_MS_MAX) }
            confirmVerified(repository)
        }

    @Test
    fun `下限未満はIllegalArgumentExceptionをスローする`() =
        runTest {
            assertFailsWith<IllegalArgumentException> {
                SaveSpeechRecognitionResumeDelayUseCase(repository)(SPEECH_RECOGNITION_RESUME_DELAY_MS_MIN - 1)
            }

            confirmVerified(repository)
        }

    @Test
    fun `上限超はIllegalArgumentExceptionをスローする`() =
        runTest {
            assertFailsWith<IllegalArgumentException> {
                SaveSpeechRecognitionResumeDelayUseCase(repository)(SPEECH_RECOGNITION_RESUME_DELAY_MS_MAX + 1)
            }

            confirmVerified(repository)
        }
}
