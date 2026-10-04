package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface Gt7Ps5MyBestLapPreferencesRepository {
    fun observeReadoutText(): Flow<String>

    suspend fun saveReadoutText(text: String)
}
