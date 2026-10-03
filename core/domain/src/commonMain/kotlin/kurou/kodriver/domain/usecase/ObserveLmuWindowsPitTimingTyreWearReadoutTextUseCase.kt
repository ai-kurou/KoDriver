package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsPitTimingPreferencesRepository

class ObserveLmuWindowsPitTimingTyreWearReadoutTextUseCase(
    private val repository: LmuWindowsPitTimingPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeTyreWearReadoutText()
}
