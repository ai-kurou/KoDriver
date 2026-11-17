package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey

public interface LmuWindowsFlagPreferencesRepository {
    public suspend fun resetToDefault()

    public fun observeFlagEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>

    public suspend fun saveFlagEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    )
}
