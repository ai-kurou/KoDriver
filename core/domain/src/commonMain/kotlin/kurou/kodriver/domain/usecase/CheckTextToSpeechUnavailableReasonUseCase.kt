package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.TextToSpeechUnavailableReason
import kurou.kodriver.domain.repository.TextToSpeechRepository

class CheckTextToSpeechUnavailableReasonUseCase(
    private val repository: TextToSpeechRepository,
) {
    suspend operator fun invoke(): TextToSpeechUnavailableReason? = repository.unavailableReason()
}
