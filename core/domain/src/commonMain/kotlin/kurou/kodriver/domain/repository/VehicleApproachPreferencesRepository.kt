package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface VehicleApproachPreferencesRepository {
    fun observeSkipFirstLap(): Flow<Boolean>
    suspend fun saveSkipFirstLap(skip: Boolean)
    fun observeStartReadoutEnabled(): Flow<Boolean>
    suspend fun saveStartReadoutEnabled(enabled: Boolean)
}
