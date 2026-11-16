package kurou.kodriver.domain.acewindows.repository

import kotlinx.coroutines.flow.Flow

public interface AceWindowsRemainingFuelPreferencesRepository {
    public suspend fun resetToDefault()

    public fun observeThresholdPercentage(): Flow<Int>

    public suspend fun saveThresholdPercentage(percentage: Int)

    public fun observeReadoutText(): Flow<String>

    public suspend fun saveReadoutText(text: String)
}
