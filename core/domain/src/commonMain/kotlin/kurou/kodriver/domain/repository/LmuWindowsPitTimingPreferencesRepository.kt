package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface LmuWindowsPitTimingPreferencesRepository {
    fun observeVirtualEnergyLaps(): Flow<Int>
    suspend fun saveVirtualEnergyLaps(laps: Int)
    fun observeTyreWearLaps(): Flow<Int>
    suspend fun saveTyreWearLaps(laps: Int)
}
