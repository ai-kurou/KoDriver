package kurou.kodriver.domain.gt7ps5.usecase

import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5RemainingFuelPreferencesRepository

class SaveGt7Ps5RemainingFuelThresholdPercentageUseCase(
    private val repository: Gt7Ps5RemainingFuelPreferencesRepository,
) {
    suspend operator fun invoke(percentage: Int) = repository.saveThresholdPercentage(percentage)
}
