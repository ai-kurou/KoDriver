package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

public interface VoicePreferencesRepository {
    public fun voiceId(): Flow<String>

    public suspend fun saveVoiceId(voiceId: String)
}
