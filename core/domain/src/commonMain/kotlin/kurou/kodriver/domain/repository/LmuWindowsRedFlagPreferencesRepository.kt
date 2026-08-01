package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.RedFlagVoiceType

interface LmuWindowsRedFlagPreferencesRepository {
    fun observeVoiceType(): Flow<RedFlagVoiceType>

    suspend fun saveVoiceType(type: RedFlagVoiceType)
}
