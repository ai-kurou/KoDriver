package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface VoicePitchPreferencesRepository {
    fun voicePitch(): Flow<Float>

    suspend fun saveVoicePitch(voicePitch: Float)
}
