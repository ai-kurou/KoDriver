package kurou.kodriver.data.manualreadout

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kurou.kodriver.domain.model.SpeechRecognitionTranscript
import kurou.kodriver.domain.repository.SpeechRecognitionTranscriptRepository

/**
 * マイクが聞き取った文言をメモリに持つ。永続化はしない。
 */
internal class InMemorySpeechRecognitionTranscriptRepository : SpeechRecognitionTranscriptRepository {
    private val _transcript = MutableStateFlow(SpeechRecognitionTranscript())
    override val transcript: StateFlow<SpeechRecognitionTranscript> = _transcript.asStateFlow()

    override fun update(transcript: SpeechRecognitionTranscript) {
        _transcript.value = transcript
    }
}
