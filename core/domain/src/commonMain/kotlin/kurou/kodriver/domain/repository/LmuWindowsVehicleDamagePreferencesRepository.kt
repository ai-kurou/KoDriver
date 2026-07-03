package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey

interface LmuWindowsVehicleDamagePreferencesRepository {
    fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>
    suspend fun saveEnabledState(key: ReadoutItemKey, enabled: Boolean)
}
