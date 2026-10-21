package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingVirtualEnergyPreferencesRepository

class SaveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase(
    private val repository: LmuWindowsRemainingVirtualEnergyPreferencesRepository,
) {
    suspend operator fun invoke(percentage: Int) = repository.saveThresholdPercentage(percentage)
}
