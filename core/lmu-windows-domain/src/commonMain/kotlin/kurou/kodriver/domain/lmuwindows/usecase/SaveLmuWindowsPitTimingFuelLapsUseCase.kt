package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitTimingPreferencesRepository

public class SaveLmuWindowsPitTimingFuelLapsUseCase(
    private val repository: LmuWindowsPitTimingPreferencesRepository,
) {
    public suspend operator fun invoke(laps: Int): Unit = repository.saveFuelLaps(laps)
}
