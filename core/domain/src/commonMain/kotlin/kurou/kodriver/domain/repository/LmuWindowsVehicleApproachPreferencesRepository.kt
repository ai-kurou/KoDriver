package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType

interface LmuWindowsVehicleApproachPreferencesRepository {
    fun observeSkipFirstLap(): Flow<Boolean>
    suspend fun saveSkipFirstLap(skip: Boolean)
    fun observeStartReadoutEnabled(): Flow<Boolean>
    suspend fun saveStartReadoutEnabled(enabled: Boolean)
    fun observeStartReadoutType(): Flow<VehicleApproachStartReadoutType>
    suspend fun saveStartReadoutType(type: VehicleApproachStartReadoutType)
    fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>
    suspend fun saveEnabledState(key: ReadoutItemKey, enabled: Boolean)
}
