package kurou.kodriver.domain.acewindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey

interface AceWindowsVehicleApproachPreferencesRepository {
    suspend fun resetToDefault()

    fun observeReadoutText(): Flow<String>

    suspend fun saveReadoutText(text: String)

    fun observeThresholdMeters(): Flow<Double>

    suspend fun saveThresholdMeters(meters: Double)

    fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>

    suspend fun saveEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    )
}
