package kurou.kodriver.domain.speechrecognition.usecase

import kotlinx.coroutines.flow.StateFlow
import kurou.kodriver.domain.speechrecognition.repository.SpeechRecognitionEnabledRepository

public class ObserveSpeechRecognitionEnabledUseCase(
    private val repository: SpeechRecognitionEnabledRepository,
) {
    public operator fun invoke(): StateFlow<Boolean> = repository.enabled
}
