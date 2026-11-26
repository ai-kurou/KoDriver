package kurou.kodriver.domain.speechrecognition.usecase

import kurou.kodriver.domain.speechrecognition.model.SpeechRecognitionTranscript
import kurou.kodriver.domain.speechrecognition.repository.SpeechRecognitionTranscriptRepository

public class ReportSpeechRecognitionTranscriptUseCase(
    private val repository: SpeechRecognitionTranscriptRepository,
) {
    public operator fun invoke(transcript: SpeechRecognitionTranscript): Unit = repository.update(transcript)
}
