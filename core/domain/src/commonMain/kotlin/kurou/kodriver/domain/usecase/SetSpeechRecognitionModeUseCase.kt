package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.SpeechRecognitionMode
import kurou.kodriver.domain.repository.SpeechRecognitionRepository

class SetSpeechRecognitionModeUseCase(
    private val repository: SpeechRecognitionRepository,
) {
    operator fun invoke(mode: SpeechRecognitionMode) = repository.setMode(mode)
}
