package kurou.kodriver.domain.acewindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey

public interface AceWindowsVehicleApproachPreferencesRepository {
    public suspend fun resetToDefault()

    public fun observeReadoutText(): Flow<String>

    public suspend fun saveReadoutText(text: String)

    public fun observeThresholdMeters(): Flow<Double>

    public suspend fun saveThresholdMeters(meters: Double)

    public fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>

    public suspend fun saveEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    )
}
