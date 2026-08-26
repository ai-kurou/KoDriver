package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.HapticFeedbackAvailabilityRepository

class CheckHapticFeedbackAvailableUseCase(
    private val repository: HapticFeedbackAvailabilityRepository,
) {
    operator fun invoke(): Boolean = repository.isHapticFeedbackAvailable()
}
