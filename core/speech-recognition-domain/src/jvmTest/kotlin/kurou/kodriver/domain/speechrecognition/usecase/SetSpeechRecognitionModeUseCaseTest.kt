package kurou.kodriver.domain.speechrecognition.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.domain.speechrecognition.model.SpeechRecognitionMode
import kurou.kodriver.domain.speechrecognition.repository.SpeechRecognitionRepository
import kotlin.test.Test

class SetSpeechRecognitionModeUseCaseTest {
    private val repository: SpeechRecognitionRepository = mockk()

    @Test
    fun `指定したモードをRepositoryへ渡す`() {
        every { repository.setMode(SpeechRecognitionMode.Command) } returns Unit

        SetSpeechRecognitionModeUseCase(repository)(SpeechRecognitionMode.Command)

        verify(exactly = 1) { repository.setMode(SpeechRecognitionMode.Command) }
        confirmVerified(repository)
    }
}
