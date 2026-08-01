package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey

interface QueuePreferencesRepository {
    fun observeQueueEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>

    suspend fun saveQueueEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    )
}
