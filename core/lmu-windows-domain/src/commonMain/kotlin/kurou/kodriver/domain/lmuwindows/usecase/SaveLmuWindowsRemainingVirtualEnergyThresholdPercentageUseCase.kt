package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingVirtualEnergyPreferencesRepository

public class SaveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase(
    private val repository: LmuWindowsRemainingVirtualEnergyPreferencesRepository,
) {
    public suspend operator fun invoke(percentage: Int): Unit = repository.saveThresholdPercentage(percentage)
}
