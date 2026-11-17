package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey

public interface LmuWindowsPitTimingPreferencesRepository {
    public suspend fun resetToDefault()

    public fun observeVirtualEnergyLaps(): Flow<Int>

    public suspend fun saveVirtualEnergyLaps(laps: Int)

    public fun observeTyreWearLaps(): Flow<Int>

    public fun observeFuelLaps(): Flow<Int>

    public suspend fun saveTyreWearLaps(laps: Int)

    public suspend fun saveFuelLaps(laps: Int)

    public fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>

    public suspend fun saveEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    )
}
