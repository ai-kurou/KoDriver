package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreWearPreferencesRepository

class SaveLmuWindowsTyreWearThresholdPercentageUseCase(
    private val repository: LmuWindowsTyreWearPreferencesRepository,
) {
    suspend operator fun invoke(percentage: Int) = repository.saveThresholdPercentage(percentage)
}
