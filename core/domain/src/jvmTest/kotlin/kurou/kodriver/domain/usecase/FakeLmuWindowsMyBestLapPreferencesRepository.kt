package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapPreferencesRepository

class FakeLmuWindowsMyBestLapPreferencesRepository(
    initialVoiceType: MyBestLapVoiceType = MyBestLapVoiceType.FORMAL,
) : LmuWindowsMyBestLapPreferencesRepository {
    private val voiceType = MutableStateFlow(initialVoiceType)

    override fun observeVoiceType(): Flow<MyBestLapVoiceType> = voiceType

    override suspend fun saveVoiceType(type: MyBestLapVoiceType) {
        voiceType.value = type
    }
}
