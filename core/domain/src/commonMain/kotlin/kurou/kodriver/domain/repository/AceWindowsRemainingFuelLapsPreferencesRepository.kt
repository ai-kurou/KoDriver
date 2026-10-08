package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface AceWindowsRemainingFuelLapsPreferencesRepository {
    fun observeThresholdLaps(): Flow<Int>

    suspend fun saveThresholdLaps(laps: Int)

    fun observeReadoutText(): Flow<String>

    suspend fun saveReadoutText(text: String)

    fun observeEmptyReadoutText(): Flow<String>

    suspend fun saveEmptyReadoutText(text: String)
}
