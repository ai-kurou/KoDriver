package kurou.kodriver.domain.speechrecognition.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.speechrecognition.model.SpeechRecognitionEvent
import kurou.kodriver.domain.speechrecognition.repository.SpeechRecognitionRepository

public class ObserveSpeechRecognitionUseCase(
    private val repository: SpeechRecognitionRepository,
) {
    public operator fun invoke(): Flow<SpeechRecognitionEvent> = repository.observeRecognition()
}
