package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.ReadoutCatalog
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.QueuePreferencesRepository

class ObserveQueueEnabledStatesUseCase(
    private val repository: QueuePreferencesRepository,
    private val catalog: ReadoutCatalog,
) {
    operator fun invoke(): Flow<Map<ReadoutItemKey, Boolean>> =
        repository.observeQueueEnabledStates().map { persisted -> catalog.queueEnabledStateDefaults + persisted }
}
