package kurou.kodriver.domain.acewindows.repository

import kotlinx.coroutines.flow.Flow

public interface AceWindowsRemainingFuelLapsPreferencesRepository {
    public suspend fun resetToDefault()

    public fun observeThresholdLaps(): Flow<Int>

    public suspend fun saveThresholdLaps(laps: Int)

    public fun observeReadoutText(): Flow<String>

    public suspend fun saveReadoutText(text: String)

    public fun observeEmptyReadoutText(): Flow<String>

    public suspend fun saveEmptyReadoutText(text: String)
}
