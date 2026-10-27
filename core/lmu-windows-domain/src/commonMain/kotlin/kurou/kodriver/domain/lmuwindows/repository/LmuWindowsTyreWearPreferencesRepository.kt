package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow

interface LmuWindowsTyreWearPreferencesRepository {
    suspend fun resetToDefault()

    fun observeThresholdPercentage(): Flow<Int>

    suspend fun saveThresholdPercentage(percentage: Int)

    fun observeReadoutText(): Flow<String>

    suspend fun saveReadoutText(text: String)
}
