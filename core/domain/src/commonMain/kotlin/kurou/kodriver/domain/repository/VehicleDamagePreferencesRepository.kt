package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey

interface VehicleDamagePreferencesRepository {
    fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>
    suspend fun saveEnabledState(key: ReadoutItemKey, enabled: Boolean)
}
