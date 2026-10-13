package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.AutoReadoutPreferencesRepository

class SaveAutoReadoutOrderUseCase(
    private val repository: AutoReadoutPreferencesRepository,
) {
    suspend operator fun invoke(
        simulator: String,
        order: List<ReadoutItemKey>,
    ) = repository.saveAutoReadoutOrder(simulator, order)
}
