package kurou.kodriver.domain.acewindows.usecase

import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelLapsPreferencesRepository

class SaveAceWindowsRemainingFuelLapsThresholdUseCase(
    private val repository: AceWindowsRemainingFuelLapsPreferencesRepository,
) {
    suspend operator fun invoke(laps: Int) = repository.saveThresholdLaps(laps)
}
