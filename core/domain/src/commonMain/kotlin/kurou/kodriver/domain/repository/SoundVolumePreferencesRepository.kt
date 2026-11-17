package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

public interface SoundVolumePreferencesRepository {
    public fun volume(): Flow<Int>

    public suspend fun saveVolume(volume: Int)
}
