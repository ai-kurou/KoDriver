package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.HapticFeedbackEnabledRepository

class SaveHapticFeedbackEnabledUseCase(
    private val repository: HapticFeedbackEnabledRepository,
) {
    suspend operator fun invoke(enabled: Boolean) = repository.saveHapticFeedbackEnabled(enabled)
}
