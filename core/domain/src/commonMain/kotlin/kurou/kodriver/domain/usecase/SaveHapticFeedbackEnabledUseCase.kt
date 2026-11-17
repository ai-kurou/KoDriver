package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.HapticFeedbackEnabledRepository

public class SaveHapticFeedbackEnabledUseCase(
    private val repository: HapticFeedbackEnabledRepository,
) {
    public suspend operator fun invoke(enabled: Boolean): Unit = repository.saveHapticFeedbackEnabled(enabled)
}
