package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.model.ReadoutItemKey

interface LmuWindowsFlagPreferencesRepository {
    fun observeFlagEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>

    suspend fun saveFlagEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    )
}
