package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow

interface LmuWindowsMyBestLapPreferencesRepository {
    suspend fun resetToDefault()

    fun observeReadoutText(): Flow<String>

    suspend fun saveReadoutText(text: String)
}
