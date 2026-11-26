package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository

public class AddManualReadoutSetUseCase(
    private val repository: ManualReadoutSetPreferencesRepository,
) {
    public suspend operator fun invoke(set: ManualReadoutSet): Unit = repository.addSet(set)
}
