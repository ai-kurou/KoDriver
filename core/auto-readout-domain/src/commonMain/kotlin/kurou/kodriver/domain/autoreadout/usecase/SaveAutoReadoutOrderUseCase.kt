package kurou.kodriver.domain.autoreadout.usecase

import kurou.kodriver.domain.autoreadout.repository.AutoReadoutPreferencesRepository
import kurou.kodriver.domain.model.ReadoutItemKey

public class SaveAutoReadoutOrderUseCase(
    private val repository: AutoReadoutPreferencesRepository,
) {
    public suspend operator fun invoke(
        simulator: String,
        order: List<ReadoutItemKey>,
    ): Unit = repository.saveAutoReadoutOrder(simulator, order)
}
