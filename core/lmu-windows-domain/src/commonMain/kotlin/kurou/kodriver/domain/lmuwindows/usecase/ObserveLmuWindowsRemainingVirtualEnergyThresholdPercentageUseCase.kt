package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingVirtualEnergyPreferencesRepository

public class ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase(
    private val repository: LmuWindowsRemainingVirtualEnergyPreferencesRepository,
) {
    public operator fun invoke(): Flow<Int> = repository.observeThresholdPercentage()
}
