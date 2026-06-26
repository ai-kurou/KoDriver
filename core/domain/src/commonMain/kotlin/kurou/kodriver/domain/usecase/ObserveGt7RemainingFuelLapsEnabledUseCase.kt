package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.Gt7RemainingFuelLapsPreferencesRepository

class ObserveGt7RemainingFuelLapsEnabledUseCase(
    private val repository: Gt7RemainingFuelLapsPreferencesRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.observeEnabled()
}
