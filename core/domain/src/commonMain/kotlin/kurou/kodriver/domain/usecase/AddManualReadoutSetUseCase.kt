package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository

class AddManualReadoutSetUseCase(
    private val repository: ManualReadoutSetPreferencesRepository,
) {
    suspend operator fun invoke(set: ManualReadoutSet) = repository.addSet(set)
}
