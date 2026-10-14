package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository

class UpdateManualReadoutSetUseCase(
    private val repository: ManualReadoutSetPreferencesRepository,
) {
    suspend operator fun invoke(set: ManualReadoutSet) = repository.updateSet(set)
}
