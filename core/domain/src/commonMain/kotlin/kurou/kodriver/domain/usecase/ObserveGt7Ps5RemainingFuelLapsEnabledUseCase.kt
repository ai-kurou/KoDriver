package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsEnabledRepository

class ObserveGt7Ps5RemainingFuelLapsEnabledUseCase(
    private val repository: Gt7Ps5RemainingFuelLapsEnabledRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.observeEnabled()
}
