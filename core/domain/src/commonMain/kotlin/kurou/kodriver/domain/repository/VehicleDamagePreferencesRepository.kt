package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface VehicleDamagePreferencesRepository {
    fun observeEnabledStates(): Flow<Map<String, Boolean>>
    suspend fun saveEnabledState(key: String, enabled: Boolean)
}
