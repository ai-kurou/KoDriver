package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey

interface FlagPreferencesRepository {
    fun observeFlagEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>
    suspend fun saveFlagEnabledState(key: ReadoutItemKey, enabled: Boolean)
}
