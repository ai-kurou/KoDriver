package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.HapticFeedbackEnabledRepository

public class ObserveHapticFeedbackEnabledUseCase(
    private val repository: HapticFeedbackEnabledRepository,
) {
    public operator fun invoke(): Flow<Boolean> = repository.hapticFeedbackEnabled()
}
