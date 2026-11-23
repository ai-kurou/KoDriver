package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kurou.kodriver.domain.model.SpeechRecognitionTranscript
import kurou.kodriver.domain.repository.SpeechRecognitionTranscriptRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveSpeechRecognitionTranscriptUseCaseTest {
    private val repository: SpeechRecognitionTranscriptRepository = mockk()

    @Test
    fun `Repositoryの聞き取り状況をそのまま返す`() {
        val state = MutableStateFlow(SpeechRecognitionTranscript(partialText = "燃"))
        every { repository.transcript } returns state

        assertEquals(state, ObserveSpeechRecognitionTranscriptUseCase(repository)())
        verify(exactly = 1) { repository.transcript }
        confirmVerified(repository)
    }
}
