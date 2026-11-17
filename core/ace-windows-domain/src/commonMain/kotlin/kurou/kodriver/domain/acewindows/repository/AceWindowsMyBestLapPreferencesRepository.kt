package kurou.kodriver.domain.acewindows.repository

import kotlinx.coroutines.flow.Flow

public interface AceWindowsMyBestLapPreferencesRepository {
    public suspend fun resetToDefault()

    public fun observeReadoutText(): Flow<String>

    public suspend fun saveReadoutText(text: String)
}
