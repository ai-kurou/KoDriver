package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.SpeechRecognitionEvent
import kurou.kodriver.domain.repository.SpeechRecognitionRepository

public class ObserveSpeechRecognitionUseCase(
    private val repository: SpeechRecognitionRepository,
) {
    public operator fun invoke(): Flow<SpeechRecognitionEvent> = repository.observeRecognition()
}
