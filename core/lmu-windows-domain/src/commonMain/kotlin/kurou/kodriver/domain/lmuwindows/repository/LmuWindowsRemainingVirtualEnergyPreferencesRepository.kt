package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow

interface LmuWindowsRemainingVirtualEnergyPreferencesRepository {
    fun observeThresholdPercentage(): Flow<Int>

    suspend fun saveThresholdPercentage(percentage: Int)

    fun observeReadoutText(): Flow<String>

    suspend fun saveReadoutText(text: String)
}
