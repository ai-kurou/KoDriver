package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface SoundVolumePreferencesRepository {
    fun volume(): Flow<Int>
    suspend fun saveVolume(volume: Int)
}
