package kurou.kodriver.domain.acewindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey

interface AceWindowsFlagPreferencesRepository {
    suspend fun resetToDefault()

    fun observeFlagEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>

    suspend fun saveFlagEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    )
}
