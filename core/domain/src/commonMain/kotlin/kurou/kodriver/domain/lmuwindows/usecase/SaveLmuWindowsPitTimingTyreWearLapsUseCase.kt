package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitTimingPreferencesRepository

class SaveLmuWindowsPitTimingTyreWearLapsUseCase(
    private val repository: LmuWindowsPitTimingPreferencesRepository,
) {
    suspend operator fun invoke(laps: Int) = repository.saveTyreWearLaps(laps)
}
