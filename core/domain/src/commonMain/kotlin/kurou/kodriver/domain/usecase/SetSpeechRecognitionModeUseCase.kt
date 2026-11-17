package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.SpeechRecognitionMode
import kurou.kodriver.domain.repository.SpeechRecognitionRepository

public class SetSpeechRecognitionModeUseCase(
    private val repository: SpeechRecognitionRepository,
) {
    public operator fun invoke(mode: SpeechRecognitionMode): Unit = repository.setMode(mode)
}
