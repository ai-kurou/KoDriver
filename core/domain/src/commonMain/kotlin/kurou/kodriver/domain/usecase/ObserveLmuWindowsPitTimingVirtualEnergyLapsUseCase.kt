package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsPitTimingPreferencesRepository

class ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase(
    private val repository: LmuWindowsPitTimingPreferencesRepository,
) {
    operator fun invoke(): Flow<Int> = repository.observeVirtualEnergyLaps()
}
