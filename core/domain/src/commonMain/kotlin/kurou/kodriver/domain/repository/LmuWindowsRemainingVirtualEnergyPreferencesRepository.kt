package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface LmuWindowsRemainingVirtualEnergyPreferencesRepository {
    fun observeThresholdPercentage(): Flow<Int>
    suspend fun saveThresholdPercentage(percentage: Int)
}
