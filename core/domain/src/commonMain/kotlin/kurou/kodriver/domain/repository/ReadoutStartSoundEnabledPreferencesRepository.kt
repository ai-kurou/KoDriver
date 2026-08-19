package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey

interface ReadoutStartSoundEnabledPreferencesRepository {
    fun observeStartSoundEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>

    suspend fun saveStartSoundEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    )
}
