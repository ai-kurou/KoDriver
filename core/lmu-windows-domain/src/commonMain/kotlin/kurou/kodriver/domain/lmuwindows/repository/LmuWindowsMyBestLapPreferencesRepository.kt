package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow

public interface LmuWindowsMyBestLapPreferencesRepository {
    public suspend fun resetToDefault()

    public fun observeReadoutText(): Flow<String>

    public suspend fun saveReadoutText(text: String)
}
