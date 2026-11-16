package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelLapsPreferencesRepository

public class ObserveAceWindowsRemainingFuelLapsThresholdUseCase(
    private val repository: AceWindowsRemainingFuelLapsPreferencesRepository,
) {
    public operator fun invoke(): Flow<Int> = repository.observeThresholdLaps()
}
