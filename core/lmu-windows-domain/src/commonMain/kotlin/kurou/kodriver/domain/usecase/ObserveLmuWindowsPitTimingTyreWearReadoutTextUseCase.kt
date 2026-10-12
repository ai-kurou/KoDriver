package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsPitTimingReadoutTextPreferencesRepository

class ObserveLmuWindowsPitTimingTyreWearReadoutTextUseCase(
    private val repository: LmuWindowsPitTimingReadoutTextPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeTyreWearReadoutText()
}
