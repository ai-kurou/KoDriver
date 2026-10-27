package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey

interface LmuWindowsPitTimingPreferencesRepository {
    suspend fun resetToDefault()

    fun observeVirtualEnergyLaps(): Flow<Int>

    suspend fun saveVirtualEnergyLaps(laps: Int)

    fun observeTyreWearLaps(): Flow<Int>

    fun observeFuelLaps(): Flow<Int>

    suspend fun saveTyreWearLaps(laps: Int)

    suspend fun saveFuelLaps(laps: Int)

    fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>

    suspend fun saveEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    )
}
