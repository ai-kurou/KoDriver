package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.LmuWindowsRemainingVirtualEnergyPreferencesRepository

class SaveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase(
    private val repository: LmuWindowsRemainingVirtualEnergyPreferencesRepository,
) {
    suspend operator fun invoke(percentage: Int) = repository.saveThresholdPercentage(percentage)
}
