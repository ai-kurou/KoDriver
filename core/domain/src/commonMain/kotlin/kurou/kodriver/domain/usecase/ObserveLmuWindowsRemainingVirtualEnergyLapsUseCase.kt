package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsRemainingVirtualEnergyLapsPreferencesRepository

class ObserveLmuWindowsRemainingVirtualEnergyLapsUseCase(
    private val repository: LmuWindowsRemainingVirtualEnergyLapsPreferencesRepository,
) {
    operator fun invoke(): Flow<Int> = repository.observeRemainingVirtualEnergyLaps()
}
