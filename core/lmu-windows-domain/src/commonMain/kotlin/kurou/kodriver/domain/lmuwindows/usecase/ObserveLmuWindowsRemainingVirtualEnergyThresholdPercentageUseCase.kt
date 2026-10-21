package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingVirtualEnergyPreferencesRepository

class ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase(
    private val repository: LmuWindowsRemainingVirtualEnergyPreferencesRepository,
) {
    operator fun invoke(): Flow<Int> = repository.observeThresholdPercentage()
}
