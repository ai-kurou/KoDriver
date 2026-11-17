package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey

public interface ReadoutStartSoundEnabledPreferencesRepository {
    public suspend fun resetToDefault()

    public fun observeStartSoundEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>

    public suspend fun saveStartSoundEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    )
}
