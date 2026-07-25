package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.LmuWindowsPitTimingPreferencesRepository

class SaveLmuWindowsPitTimingVirtualEnergyLapsUseCase(
    private val repository: LmuWindowsPitTimingPreferencesRepository,
) {
    suspend operator fun invoke(laps: Int) = repository.saveVirtualEnergyLaps(laps)
}
