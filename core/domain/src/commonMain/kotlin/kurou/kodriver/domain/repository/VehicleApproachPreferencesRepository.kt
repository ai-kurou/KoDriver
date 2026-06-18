package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType

interface VehicleApproachPreferencesRepository {
    fun observeSkipFirstLap(): Flow<Boolean>
    suspend fun saveSkipFirstLap(skip: Boolean)
    fun observeStartReadoutEnabled(): Flow<Boolean>
    suspend fun saveStartReadoutEnabled(enabled: Boolean)
    fun observeStartReadoutType(): Flow<VehicleApproachStartReadoutType>
    suspend fun saveStartReadoutType(type: VehicleApproachStartReadoutType)
}
