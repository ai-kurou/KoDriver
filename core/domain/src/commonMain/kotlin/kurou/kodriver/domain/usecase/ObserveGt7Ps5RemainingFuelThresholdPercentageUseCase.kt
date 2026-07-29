package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelPreferencesRepository

class ObserveGt7Ps5RemainingFuelThresholdPercentageUseCase(
    private val repository: Gt7Ps5RemainingFuelPreferencesRepository,
) {
    operator fun invoke(): Flow<Int> = repository.observeThresholdPercentage()
}
