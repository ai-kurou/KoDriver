package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreWearPreferencesRepository

public class SaveLmuWindowsTyreWearThresholdPercentageUseCase(
    private val repository: LmuWindowsTyreWearPreferencesRepository,
) {
    public suspend operator fun invoke(percentage: Int): Unit = repository.saveThresholdPercentage(percentage)
}
