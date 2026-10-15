package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository

class SaveManualReadoutSetsUseCase(
    private val repository: ManualReadoutSetPreferencesRepository,
) {
    suspend operator fun invoke(sets: List<ManualReadoutSet>) = repository.saveSets(sets)
}
