package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.StateFlow
import kurou.kodriver.domain.model.SpeechRecognitionTranscript
import kurou.kodriver.domain.repository.SpeechRecognitionTranscriptRepository

public class ObserveSpeechRecognitionTranscriptUseCase(
    private val repository: SpeechRecognitionTranscriptRepository,
) {
    public operator fun invoke(): StateFlow<SpeechRecognitionTranscript> = repository.transcript
}
