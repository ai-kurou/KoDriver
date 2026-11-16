package kurou.kodriver.domain.acewindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey

public interface AceWindowsFlagPreferencesRepository {
    public suspend fun resetToDefault()

    public fun observeFlagEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>

    public suspend fun saveFlagEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    )
}
