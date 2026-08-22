package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey

interface AceWindowsVehicleApproachPreferencesRepository {
    fun observeThresholdMeters(): Flow<Double>

    suspend fun saveThresholdMeters(meters: Double)

    fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>

    suspend fun saveEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    )
}
