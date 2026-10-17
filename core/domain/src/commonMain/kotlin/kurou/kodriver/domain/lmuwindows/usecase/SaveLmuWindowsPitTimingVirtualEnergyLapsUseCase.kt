package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitTimingPreferencesRepository

class SaveLmuWindowsPitTimingVirtualEnergyLapsUseCase(
    private val repository: LmuWindowsPitTimingPreferencesRepository,
) {
    suspend operator fun invoke(laps: Int) = repository.saveVirtualEnergyLaps(laps)
}
