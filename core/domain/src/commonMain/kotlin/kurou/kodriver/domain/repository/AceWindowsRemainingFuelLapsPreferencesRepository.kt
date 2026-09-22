package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface AceWindowsRemainingFuelLapsPreferencesRepository {
    fun observeThresholdLaps(): Flow<Int>

    suspend fun saveThresholdLaps(laps: Int)
}
