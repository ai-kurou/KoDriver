package kurou.kodriver.domain.gt7ps5.usecase

import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository

class SaveGt7Ps5RemainingFuelLapsUseCase(
    private val repository: Gt7Ps5RemainingFuelLapsPreferencesRepository,
) {
    suspend operator fun invoke(laps: Int) = repository.saveRemainingFuelLaps(laps)
}
