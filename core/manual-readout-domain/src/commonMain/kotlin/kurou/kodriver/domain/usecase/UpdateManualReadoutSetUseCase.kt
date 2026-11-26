package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository

public class UpdateManualReadoutSetUseCase(
    private val repository: ManualReadoutSetPreferencesRepository,
) {
    public suspend operator fun invoke(set: ManualReadoutSet): Unit = repository.updateSet(set)
}
