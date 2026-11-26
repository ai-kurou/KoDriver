package kurou.kodriver.data.manualreadout

import kurou.kodriver.domain.speechrecognition.model.SpeechRecognitionTranscript
import kotlin.test.Test
import kotlin.test.assertEquals

class InMemorySpeechRecognitionTranscriptRepositoryTest {
    private val repository = InMemorySpeechRecognitionTranscriptRepository()

    @Test
    fun `初期状態は何も聞き取っていない`() {
        assertEquals(SpeechRecognitionTranscript(), repository.transcript.value)
    }

    @Test
    fun `更新した聞き取り状況を購読側が受け取る`() {
        val transcript = SpeechRecognitionTranscript(partialText = "燃", recognizedText = "燃料")

        repository.update(transcript)

        assertEquals(transcript, repository.transcript.value)
    }

    @Test
    fun `空の状態で更新すると消える`() {
        repository.update(SpeechRecognitionTranscript(partialText = "燃", recognizedText = "燃料"))

        repository.update(SpeechRecognitionTranscript())

        assertEquals(SpeechRecognitionTranscript(), repository.transcript.value)
    }
}
