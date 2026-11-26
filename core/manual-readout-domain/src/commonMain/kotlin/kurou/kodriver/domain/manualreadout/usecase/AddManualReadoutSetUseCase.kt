package kurou.kodriver.domain.manualreadout.usecase

import kurou.kodriver.domain.manualreadout.repository.ManualReadoutSetPreferencesRepository
import kurou.kodriver.domain.model.ManualReadoutSet

public class AddManualReadoutSetUseCase(
    private val repository: ManualReadoutSetPreferencesRepository,
) {
    public suspend operator fun invoke(set: ManualReadoutSet): Unit = repository.addSet(set)
}
