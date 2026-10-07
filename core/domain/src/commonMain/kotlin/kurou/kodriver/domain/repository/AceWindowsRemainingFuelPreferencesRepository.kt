package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface AceWindowsRemainingFuelPreferencesRepository {
    fun observeThresholdPercentage(): Flow<Int>

    suspend fun saveThresholdPercentage(percentage: Int)

    fun observeReadoutText(): Flow<String>

    suspend fun saveReadoutText(text: String)
}
