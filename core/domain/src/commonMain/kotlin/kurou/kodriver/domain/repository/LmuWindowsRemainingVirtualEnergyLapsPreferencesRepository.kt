package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface LmuWindowsRemainingVirtualEnergyLapsPreferencesRepository {
    fun observeRemainingVirtualEnergyLaps(): Flow<Int>
    suspend fun saveRemainingVirtualEnergyLaps(laps: Int)
}
