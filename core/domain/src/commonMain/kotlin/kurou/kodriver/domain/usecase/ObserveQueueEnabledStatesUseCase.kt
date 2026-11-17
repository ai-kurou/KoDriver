package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.QUEUE_ENABLED_STATE_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.QueuePreferencesRepository

public class ObserveQueueEnabledStatesUseCase(
    private val repository: QueuePreferencesRepository,
) {
    public operator fun invoke(): Flow<Map<ReadoutItemKey, Boolean>> =
        repository.observeQueueEnabledStates().map { persisted -> QUEUE_ENABLED_STATE_DEFAULT + persisted }
}
