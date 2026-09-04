package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.OverheatVoiceType

interface LmuWindowsOverheatPreferencesRepository {
    fun observeVoiceType(): Flow<OverheatVoiceType>

    suspend fun saveVoiceType(type: OverheatVoiceType)
}
