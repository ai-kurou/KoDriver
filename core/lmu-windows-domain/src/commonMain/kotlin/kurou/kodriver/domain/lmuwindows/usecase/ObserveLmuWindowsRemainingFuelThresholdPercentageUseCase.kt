package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingFuelPreferencesRepository

public class ObserveLmuWindowsRemainingFuelThresholdPercentageUseCase(
    private val repository: LmuWindowsRemainingFuelPreferencesRepository,
) {
    public operator fun invoke(): Flow<Int> = repository.observeThresholdPercentage()
}
