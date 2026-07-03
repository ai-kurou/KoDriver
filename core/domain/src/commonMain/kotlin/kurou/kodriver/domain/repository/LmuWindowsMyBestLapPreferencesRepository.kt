package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.MyBestLapVoiceType

interface LmuWindowsMyBestLapPreferencesRepository {
    fun observeVoiceType(): Flow<MyBestLapVoiceType>
    suspend fun saveVoiceType(type: MyBestLapVoiceType)
}
