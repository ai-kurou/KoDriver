package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.AutoReadoutPreferencesRepository

public class SaveAutoReadoutOrderUseCase(
    private val repository: AutoReadoutPreferencesRepository,
) {
    public suspend operator fun invoke(
        simulator: String,
        order: List<ReadoutItemKey>,
    ): Unit = repository.saveAutoReadoutOrder(simulator, order)
}
