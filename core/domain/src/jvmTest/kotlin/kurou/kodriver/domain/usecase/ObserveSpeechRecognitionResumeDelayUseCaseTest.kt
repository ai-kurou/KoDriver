package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.SpeechRecognitionResumeDelayPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveSpeechRecognitionResumeDelayUseCaseTest {
    private val repository: SpeechRecognitionResumeDelayPreferencesRepository = mockk()

    @Test
    fun `保存済みの待ち時間を返す`() =
        runTest {
            every { repository.resumeDelayMs() } returns flowOf(1_500)

            assertEquals(1_500, ObserveSpeechRecognitionResumeDelayUseCase(repository)().first())

            verify(exactly = 1) { repository.resumeDelayMs() }
            confirmVerified(repository)
        }
}
