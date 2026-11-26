package kurou.kodriver.domain.speechrecognition.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.domain.speechrecognition.model.SpeechRecognitionTranscript
import kurou.kodriver.domain.speechrecognition.repository.SpeechRecognitionTranscriptRepository
import kotlin.test.Test

class ReportSpeechRecognitionTranscriptUseCaseTest {
    private val repository: SpeechRecognitionTranscriptRepository = mockk()

    @Test
    fun `聞き取り状況をRepositoryへ渡す`() {
        val transcript = SpeechRecognitionTranscript(partialText = "燃", recognizedText = "燃料")
        every { repository.update(transcript) } returns Unit

        ReportSpeechRecognitionTranscriptUseCase(repository)(transcript)

        verify(exactly = 1) { repository.update(transcript) }
        confirmVerified(repository)
    }
}
