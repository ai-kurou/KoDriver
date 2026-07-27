package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelPreferencesRepository

class ObserveAceWindowsRemainingFuelThresholdPercentageUseCase(
    private val repository: AceWindowsRemainingFuelPreferencesRepository,
) {
    operator fun invoke(): Flow<Int> = repository.observeThresholdPercentage()
}
