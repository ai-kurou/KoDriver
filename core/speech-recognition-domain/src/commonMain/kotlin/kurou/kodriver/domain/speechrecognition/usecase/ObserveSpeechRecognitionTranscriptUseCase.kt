package kurou.kodriver.domain.speechrecognition.usecase

import kotlinx.coroutines.flow.StateFlow
import kurou.kodriver.domain.speechrecognition.model.SpeechRecognitionTranscript
import kurou.kodriver.domain.speechrecognition.repository.SpeechRecognitionTranscriptRepository

public class ObserveSpeechRecognitionTranscriptUseCase(
    private val repository: SpeechRecognitionTranscriptRepository,
) {
    public operator fun invoke(): StateFlow<SpeechRecognitionTranscript> = repository.transcript
}
