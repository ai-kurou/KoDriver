package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.TextToSpeechRepository

public class CheckTextToSpeechAvailableUseCase(
    private val repository: TextToSpeechRepository,
) {
    public suspend operator fun invoke(): Boolean = repository.isAvailable()
}
