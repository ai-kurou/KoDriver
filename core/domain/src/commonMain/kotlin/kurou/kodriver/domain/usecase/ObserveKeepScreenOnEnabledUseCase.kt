package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository

class ObserveKeepScreenOnEnabledUseCase(
    private val repository: KeepScreenOnEnabledRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.keepScreenOn()
}
