package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface VoiceSpeedPreferencesRepository {
    fun voiceSpeed(): Flow<Float>

    suspend fun saveVoiceSpeed(voiceSpeed: Float)
}
