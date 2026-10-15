package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository

class DeleteManualReadoutSetUseCase(
    private val repository: ManualReadoutSetPreferencesRepository,
) {
    suspend operator fun invoke(id: String) = repository.deleteSet(id)
}
