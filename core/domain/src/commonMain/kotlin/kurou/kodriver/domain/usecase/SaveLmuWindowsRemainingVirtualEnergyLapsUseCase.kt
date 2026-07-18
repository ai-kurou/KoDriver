package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.LmuWindowsRemainingVirtualEnergyLapsPreferencesRepository

class SaveLmuWindowsRemainingVirtualEnergyLapsUseCase(
    private val repository: LmuWindowsRemainingVirtualEnergyLapsPreferencesRepository,
) {
    suspend operator fun invoke(laps: Int) = repository.saveRemainingVirtualEnergyLaps(laps)
}
