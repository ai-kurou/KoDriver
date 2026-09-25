package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.TextToSpeechRepository

class CheckTextToSpeechAvailableUseCase(
    private val repository: TextToSpeechRepository,
) {
    suspend operator fun invoke(): Boolean = repository.isAvailable()
}
