package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository

public class ObserveKeepScreenOnEnabledUseCase(
    private val repository: KeepScreenOnEnabledRepository,
) {
    public operator fun invoke(): Flow<Boolean> = repository.keepScreenOn()
}
