package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey

public interface QueuePreferencesRepository {
    public suspend fun resetToDefault()

    public fun observeQueueEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>

    public suspend fun saveQueueEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    )
}
