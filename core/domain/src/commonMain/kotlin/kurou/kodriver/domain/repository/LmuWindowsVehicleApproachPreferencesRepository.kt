package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.model.ReadoutItemKey
import kurou.kodriver.core.model.VehicleApproachStartReadoutType
import kurou.kodriver.core.model.VehicleApproachSustainedReadoutType

interface LmuWindowsVehicleApproachPreferencesRepository {
    fun observeSkipFirstLap(): Flow<Boolean>

    suspend fun saveSkipFirstLap(skip: Boolean)

    fun observeStartReadoutType(): Flow<VehicleApproachStartReadoutType>

    suspend fun saveStartReadoutType(type: VehicleApproachStartReadoutType)

    fun observeSustainedReadoutType(): Flow<VehicleApproachSustainedReadoutType>

    suspend fun saveSustainedReadoutType(type: VehicleApproachSustainedReadoutType)

    fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>

    suspend fun saveEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    )
}
