package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.HapticFeedbackAvailabilityRepository

public class CheckHapticFeedbackAvailableUseCase(
    private val repository: HapticFeedbackAvailabilityRepository,
) {
    public operator fun invoke(): Boolean = repository.isHapticFeedbackAvailable()
}
