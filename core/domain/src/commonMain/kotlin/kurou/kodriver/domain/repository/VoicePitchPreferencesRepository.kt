package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

public interface VoicePitchPreferencesRepository {
    public fun voicePitch(): Flow<Float>

    public suspend fun saveVoicePitch(voicePitch: Float)
}
