package kurou.kodriver.domain.acewindows.repository

import kotlinx.coroutines.flow.Flow

interface AceWindowsRemainingFuelPreferencesRepository {
    suspend fun resetToDefault()

    fun observeThresholdPercentage(): Flow<Int>

    suspend fun saveThresholdPercentage(percentage: Int)

    fun observeReadoutText(): Flow<String>

    suspend fun saveReadoutText(text: String)
}
