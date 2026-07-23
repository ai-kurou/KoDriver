package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.LmuWindowsTyreWearPreferencesRepository

class SaveLmuWindowsTyreWearThresholdPercentageUseCase(
    private val repository: LmuWindowsTyreWearPreferencesRepository,
) {
    suspend operator fun invoke(percentage: Int) = repository.saveThresholdPercentage(percentage)
}
