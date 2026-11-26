package kurou.kodriver.domain.speechrecognition.usecase

import kurou.kodriver.domain.speechrecognition.model.SpeechRecognitionMode
import kurou.kodriver.domain.speechrecognition.repository.SpeechRecognitionRepository

public class SetSpeechRecognitionModeUseCase(
    private val repository: SpeechRecognitionRepository,
) {
    public operator fun invoke(mode: SpeechRecognitionMode): Unit = repository.setMode(mode)
}
