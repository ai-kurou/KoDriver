package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

public interface MicrophoneCutQuietSoundPreferencesRepository {
    public fun observeMicrophoneCutQuietSoundLevel(): Flow<Int>

    public suspend fun saveMicrophoneCutQuietSoundLevel(level: Int)
}
