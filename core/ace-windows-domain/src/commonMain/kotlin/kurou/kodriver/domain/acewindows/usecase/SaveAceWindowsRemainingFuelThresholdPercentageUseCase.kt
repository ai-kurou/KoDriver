package kurou.kodriver.domain.acewindows.usecase

import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelPreferencesRepository

public class SaveAceWindowsRemainingFuelThresholdPercentageUseCase(
    private val repository: AceWindowsRemainingFuelPreferencesRepository,
) {
    public suspend operator fun invoke(percentage: Int): Unit = repository.saveThresholdPercentage(percentage)
}
