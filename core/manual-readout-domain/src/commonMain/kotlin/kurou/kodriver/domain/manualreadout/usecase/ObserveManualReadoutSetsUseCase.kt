package kurou.kodriver.domain.manualreadout.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.manualreadout.repository.ManualReadoutSetPreferencesRepository
import kurou.kodriver.domain.model.ManualReadoutSet

public class ObserveManualReadoutSetsUseCase(
    private val repository: ManualReadoutSetPreferencesRepository,
) {
    public operator fun invoke(): Flow<List<ManualReadoutSet>> = repository.observeSets()
}
