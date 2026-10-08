package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface AceWindowsMyBestLapPreferencesRepository {
    fun observeReadoutText(): Flow<String>

    suspend fun saveReadoutText(text: String)
}
