package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey

interface LmuWindowsRainPreferencesRepository {
    fun observeRainEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>

    suspend fun saveRainEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    )
}
