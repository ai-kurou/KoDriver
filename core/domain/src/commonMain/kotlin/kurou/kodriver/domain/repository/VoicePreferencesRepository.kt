package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface VoicePreferencesRepository {
    fun voiceId(): Flow<String>

    suspend fun saveVoiceId(voiceId: String)
}
