package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.LmuWindowsPitTimingPreferencesRepository

class SaveLmuWindowsPitTimingTyreWearLapsUseCase(
    private val repository: LmuWindowsPitTimingPreferencesRepository,
) {
    suspend operator fun invoke(laps: Int) = repository.saveTyreWearLaps(laps)
}
