package kurou.kodriver.domain.manualreadout.usecase

import kurou.kodriver.domain.manualreadout.repository.ManualReadoutSetPreferencesRepository
import kurou.kodriver.domain.model.ManualReadoutSet

public class UpdateManualReadoutSetUseCase(
    private val repository: ManualReadoutSetPreferencesRepository,
) {
    public suspend operator fun invoke(set: ManualReadoutSet): Unit = repository.updateSet(set)
}
