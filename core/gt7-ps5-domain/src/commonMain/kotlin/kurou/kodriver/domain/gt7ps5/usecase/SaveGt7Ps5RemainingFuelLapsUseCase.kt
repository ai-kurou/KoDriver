package kurou.kodriver.domain.gt7ps5.usecase

import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository

public class SaveGt7Ps5RemainingFuelLapsUseCase(
    private val repository: Gt7Ps5RemainingFuelLapsPreferencesRepository,
) {
    public suspend operator fun invoke(laps: Int): Unit = repository.saveRemainingFuelLaps(laps)
}
