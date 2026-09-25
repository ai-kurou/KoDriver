package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.TextToSpeechRepository

class StopSpeakingUseCase(
    private val repository: TextToSpeechRepository,
) {
    suspend operator fun invoke() = repository.stop()
}
