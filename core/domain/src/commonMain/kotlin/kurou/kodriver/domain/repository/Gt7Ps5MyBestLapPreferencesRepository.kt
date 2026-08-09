package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.model.MyBestLapVoiceType

interface Gt7Ps5MyBestLapPreferencesRepository {
    fun observeVoiceType(): Flow<MyBestLapVoiceType>

    suspend fun saveVoiceType(type: MyBestLapVoiceType)
}
