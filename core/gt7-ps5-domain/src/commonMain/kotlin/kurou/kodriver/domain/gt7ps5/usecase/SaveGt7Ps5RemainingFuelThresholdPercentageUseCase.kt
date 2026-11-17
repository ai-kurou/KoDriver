package kurou.kodriver.domain.gt7ps5.usecase

import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5RemainingFuelPreferencesRepository

public class SaveGt7Ps5RemainingFuelThresholdPercentageUseCase(
    private val repository: Gt7Ps5RemainingFuelPreferencesRepository,
) {
    public suspend operator fun invoke(percentage: Int): Unit = repository.saveThresholdPercentage(percentage)
}
