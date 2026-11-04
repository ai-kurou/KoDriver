package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.SpeechRecognitionRepository

class CheckSpeechRecognitionAvailableUseCase(
    private val repository: SpeechRecognitionRepository,
) {
    operator fun invoke(): Boolean = repository.isAvailable()
}
