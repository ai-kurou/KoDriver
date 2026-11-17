package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitTimingPreferencesRepository

public class ObserveLmuWindowsPitTimingTyreWearLapsUseCase(
    private val repository: LmuWindowsPitTimingPreferencesRepository,
) {
    public operator fun invoke(): Flow<Int> = repository.observeTyreWearLaps()
}
