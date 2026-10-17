package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey

interface LmuWindowsFlagPreferencesRepository {
    fun observeFlagEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>

    suspend fun saveFlagEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    )
}
