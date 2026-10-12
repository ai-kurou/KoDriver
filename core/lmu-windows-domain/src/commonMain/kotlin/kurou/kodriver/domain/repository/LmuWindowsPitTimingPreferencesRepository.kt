package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey

interface LmuWindowsPitTimingPreferencesRepository {
    fun observeVirtualEnergyLaps(): Flow<Int>

    suspend fun saveVirtualEnergyLaps(laps: Int)

    fun observeTyreWearLaps(): Flow<Int>

    suspend fun saveTyreWearLaps(laps: Int)

    fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>

    suspend fun saveEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    )
}
