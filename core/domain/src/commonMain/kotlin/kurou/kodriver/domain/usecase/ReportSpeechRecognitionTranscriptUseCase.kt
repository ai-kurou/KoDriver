package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.SpeechRecognitionTranscript
import kurou.kodriver.domain.repository.SpeechRecognitionTranscriptRepository

public class ReportSpeechRecognitionTranscriptUseCase(
    private val repository: SpeechRecognitionTranscriptRepository,
) {
    public operator fun invoke(transcript: SpeechRecognitionTranscript): Unit = repository.update(transcript)
}
