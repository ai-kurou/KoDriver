package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingFuelPreferencesRepository

class ObserveLmuWindowsRemainingFuelThresholdPercentageUseCase(
    private val repository: LmuWindowsRemainingFuelPreferencesRepository,
) {
    operator fun invoke(): Flow<Int> = repository.observeThresholdPercentage()
}
