package kurou.kodriver.domain.gt7ps5.repository

import kotlinx.coroutines.flow.Flow

interface Gt7Ps5MyBestLapPreferencesRepository {
    suspend fun resetToDefault()

    fun observeReadoutText(): Flow<String>

    suspend fun saveReadoutText(text: String)
}
