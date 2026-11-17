package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

public interface VoiceSpeedPreferencesRepository {
    public fun voiceSpeed(): Flow<Float>

    public suspend fun saveVoiceSpeed(voiceSpeed: Float)
}
