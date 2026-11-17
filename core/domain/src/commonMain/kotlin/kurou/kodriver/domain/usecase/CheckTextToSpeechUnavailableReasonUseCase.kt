package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.TextToSpeechUnavailableReason
import kurou.kodriver.domain.repository.TextToSpeechRepository

public class CheckTextToSpeechUnavailableReasonUseCase(
    private val repository: TextToSpeechRepository,
) {
    public suspend operator fun invoke(): TextToSpeechUnavailableReason? = repository.unavailableReason()
}
