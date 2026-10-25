package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitTimingPreferencesRepository

class ObserveLmuWindowsPitTimingFuelLapsUseCase(
    private val repository: LmuWindowsPitTimingPreferencesRepository,
) {
    operator fun invoke(): Flow<Int> = repository.observeFuelLaps()
}
