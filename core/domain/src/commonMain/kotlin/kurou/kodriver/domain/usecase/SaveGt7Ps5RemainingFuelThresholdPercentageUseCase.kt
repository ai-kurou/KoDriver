package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelPreferencesRepository

class SaveGt7Ps5RemainingFuelThresholdPercentageUseCase(
    private val repository: Gt7Ps5RemainingFuelPreferencesRepository,
) {
    suspend operator fun invoke(percentage: Int) = repository.saveThresholdPercentage(percentage)
}
