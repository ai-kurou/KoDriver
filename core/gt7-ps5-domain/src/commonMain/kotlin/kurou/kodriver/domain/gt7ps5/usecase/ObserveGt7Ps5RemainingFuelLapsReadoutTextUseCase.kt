package kurou.kodriver.domain.gt7ps5.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository

class ObserveGt7Ps5RemainingFuelLapsReadoutTextUseCase(
    private val repository: Gt7Ps5RemainingFuelLapsPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeReadoutText()
}
