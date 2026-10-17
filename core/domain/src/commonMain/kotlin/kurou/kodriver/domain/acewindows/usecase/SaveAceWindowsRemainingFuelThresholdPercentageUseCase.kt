package kurou.kodriver.domain.acewindows.usecase

import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelPreferencesRepository

class SaveAceWindowsRemainingFuelThresholdPercentageUseCase(
    private val repository: AceWindowsRemainingFuelPreferencesRepository,
) {
    suspend operator fun invoke(percentage: Int) = repository.saveThresholdPercentage(percentage)
}
