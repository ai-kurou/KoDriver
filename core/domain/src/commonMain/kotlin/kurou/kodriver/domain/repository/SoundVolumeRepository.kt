package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface SoundVolumeRepository {
    fun volume(): Flow<Int>
    suspend fun saveVolume(volume: Int)
}
