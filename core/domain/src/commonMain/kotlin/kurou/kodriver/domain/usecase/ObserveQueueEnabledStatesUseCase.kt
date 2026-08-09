package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.core.model.ReadoutItemKey
import kurou.kodriver.domain.model.QUEUE_ENABLED_STATE_DEFAULT
import kurou.kodriver.domain.repository.QueuePreferencesRepository

class ObserveQueueEnabledStatesUseCase(
    private val repository: QueuePreferencesRepository,
) {
    operator fun invoke(): Flow<Map<ReadoutItemKey, Boolean>> =
        repository.observeQueueEnabledStates().map { persisted -> QUEUE_ENABLED_STATE_DEFAULT + persisted }
}
