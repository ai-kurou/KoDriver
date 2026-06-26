package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository

class ObserveGt7Ps5RemainingFuelLapsUseCase(
    private val repository: Gt7Ps5RemainingFuelLapsPreferencesRepository,
) {
    operator fun invoke(): Flow<Int> = repository.observeRemainingFuelLaps()
}
