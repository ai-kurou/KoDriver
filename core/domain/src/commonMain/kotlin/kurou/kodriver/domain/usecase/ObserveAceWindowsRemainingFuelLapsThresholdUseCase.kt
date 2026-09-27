package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelLapsPreferencesRepository

class ObserveAceWindowsRemainingFuelLapsThresholdUseCase(
    private val repository: AceWindowsRemainingFuelLapsPreferencesRepository,
) {
    operator fun invoke(): Flow<Int> = repository.observeThresholdLaps()
}
