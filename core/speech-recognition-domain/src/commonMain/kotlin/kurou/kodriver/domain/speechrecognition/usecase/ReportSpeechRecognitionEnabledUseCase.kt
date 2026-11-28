package kurou.kodriver.domain.speechrecognition.usecase

import kurou.kodriver.domain.speechrecognition.repository.SpeechRecognitionEnabledRepository

public class ReportSpeechRecognitionEnabledUseCase(
    private val repository: SpeechRecognitionEnabledRepository,
) {
    public operator fun invoke(enabled: Boolean): Unit = repository.update(enabled)
}
