package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.SpeechRecognitionEvent
import kurou.kodriver.domain.repository.SpeechRecognitionRepository

class ObserveSpeechRecognitionUseCase(
    private val repository: SpeechRecognitionRepository,
) {
    operator fun invoke(): Flow<SpeechRecognitionEvent> = repository.observeRecognition()
}
