package kurou.kodriver.domain.acewindows.usecase

import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelLapsPreferencesRepository

public class SaveAceWindowsRemainingFuelLapsThresholdUseCase(
    private val repository: AceWindowsRemainingFuelLapsPreferencesRepository,
) {
    public suspend operator fun invoke(laps: Int): Unit = repository.saveThresholdLaps(laps)
}
