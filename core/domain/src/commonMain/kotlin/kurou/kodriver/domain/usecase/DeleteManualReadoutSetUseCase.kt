package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository

public class DeleteManualReadoutSetUseCase(
    private val repository: ManualReadoutSetPreferencesRepository,
) {
    public suspend operator fun invoke(id: String): Unit = repository.deleteSet(id)
}
