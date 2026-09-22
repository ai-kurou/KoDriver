package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.AceWindowsRemainingFuelLapsPreferencesRepository

class SaveAceWindowsRemainingFuelLapsThresholdUseCase(
    private val repository: AceWindowsRemainingFuelLapsPreferencesRepository,
) {
    suspend operator fun invoke(laps: Int) = repository.saveThresholdLaps(laps)
}
