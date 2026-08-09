package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.model.ReadoutItemKey
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository

class ObserveReadoutOrderUseCase(
    private val repository: ReadoutPreferencesRepository,
) {
    operator fun invoke(simulator: String): Flow<List<ReadoutItemKey>> = repository.observeReadoutOrder(simulator)
}
