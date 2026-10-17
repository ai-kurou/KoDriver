package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow

interface LmuWindowsMyBestLapPreferencesRepository {
    fun observeReadoutText(): Flow<String>

    suspend fun saveReadoutText(text: String)
}
