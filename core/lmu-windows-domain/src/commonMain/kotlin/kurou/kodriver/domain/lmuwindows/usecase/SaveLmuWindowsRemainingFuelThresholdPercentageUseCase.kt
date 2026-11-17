package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingFuelPreferencesRepository

public class SaveLmuWindowsRemainingFuelThresholdPercentageUseCase(
    private val repository: LmuWindowsRemainingFuelPreferencesRepository,
) {
    public suspend operator fun invoke(percentage: Int): Unit = repository.saveThresholdPercentage(percentage)
}
