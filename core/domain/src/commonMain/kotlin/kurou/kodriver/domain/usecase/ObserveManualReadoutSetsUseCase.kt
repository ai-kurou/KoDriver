package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository

class ObserveManualReadoutSetsUseCase(
    private val repository: ManualReadoutSetPreferencesRepository,
) {
    operator fun invoke(): Flow<List<ManualReadoutSet>> = repository.observeSets()
}
