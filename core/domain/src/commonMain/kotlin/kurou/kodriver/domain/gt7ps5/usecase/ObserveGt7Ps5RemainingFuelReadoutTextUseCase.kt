package kurou.kodriver.domain.gt7ps5.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5RemainingFuelPreferencesRepository

class ObserveGt7Ps5RemainingFuelReadoutTextUseCase(
    private val repository: Gt7Ps5RemainingFuelPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeReadoutText()
}
