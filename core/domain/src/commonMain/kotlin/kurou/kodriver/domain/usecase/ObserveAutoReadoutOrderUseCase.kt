package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.AutoReadoutPreferencesRepository

class ObserveAutoReadoutOrderUseCase(
    private val repository: AutoReadoutPreferencesRepository,
) {
    operator fun invoke(simulator: String): Flow<List<ReadoutItemKey>> = repository.observeAutoReadoutOrder(simulator)
}
