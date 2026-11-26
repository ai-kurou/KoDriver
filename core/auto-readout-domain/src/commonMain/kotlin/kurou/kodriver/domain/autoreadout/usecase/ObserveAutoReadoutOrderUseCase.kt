package kurou.kodriver.domain.autoreadout.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.autoreadout.repository.AutoReadoutPreferencesRepository
import kurou.kodriver.domain.model.ReadoutItemKey

public class ObserveAutoReadoutOrderUseCase(
    private val repository: AutoReadoutPreferencesRepository,
) {
    public operator fun invoke(simulator: String): Flow<List<ReadoutItemKey>> =
        repository.observeAutoReadoutOrder(simulator)
}
