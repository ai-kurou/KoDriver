package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.AceWindowsRemainingFuelPreferencesRepository

class SaveAceWindowsRemainingFuelThresholdPercentageUseCase(
    private val repository: AceWindowsRemainingFuelPreferencesRepository,
) {
    suspend operator fun invoke(percentage: Int) = repository.saveThresholdPercentage(percentage)
}
