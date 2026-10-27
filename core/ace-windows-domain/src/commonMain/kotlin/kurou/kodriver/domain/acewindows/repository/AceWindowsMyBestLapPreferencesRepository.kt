package kurou.kodriver.domain.acewindows.repository

import kotlinx.coroutines.flow.Flow

interface AceWindowsMyBestLapPreferencesRepository {
    suspend fun resetToDefault()

    fun observeReadoutText(): Flow<String>

    suspend fun saveReadoutText(text: String)
}
