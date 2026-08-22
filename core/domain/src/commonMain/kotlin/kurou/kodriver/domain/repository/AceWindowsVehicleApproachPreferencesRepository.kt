package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType

interface AceWindowsVehicleApproachPreferencesRepository {
    fun observeLongitudinalThresholdMeters(): Flow<Double>

    suspend fun saveLongitudinalThresholdMeters(meters: Double)

    fun observeLateralThresholdMeters(): Flow<Double>

    suspend fun saveLateralThresholdMeters(meters: Double)

    fun observeStartReadoutType(): Flow<VehicleApproachStartReadoutType>

    suspend fun saveStartReadoutType(type: VehicleApproachStartReadoutType)

    fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>

    suspend fun saveEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    )
}
