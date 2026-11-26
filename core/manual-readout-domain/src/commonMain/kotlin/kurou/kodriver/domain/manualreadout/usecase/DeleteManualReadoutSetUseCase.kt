package kurou.kodriver.domain.manualreadout.usecase

import kurou.kodriver.domain.manualreadout.repository.ManualReadoutSetPreferencesRepository

public class DeleteManualReadoutSetUseCase(
    private val repository: ManualReadoutSetPreferencesRepository,
) {
    public suspend operator fun invoke(id: String): Unit = repository.deleteSet(id)
}
