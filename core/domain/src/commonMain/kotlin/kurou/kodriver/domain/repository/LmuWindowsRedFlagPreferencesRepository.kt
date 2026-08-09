package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.model.RedFlagVoiceType

interface LmuWindowsRedFlagPreferencesRepository {
    fun observeVoiceType(): Flow<RedFlagVoiceType>

    suspend fun saveVoiceType(type: RedFlagVoiceType)
}
