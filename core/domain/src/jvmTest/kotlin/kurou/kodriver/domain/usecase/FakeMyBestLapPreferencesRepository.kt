package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.repository.MyBestLapPreferencesRepository

class FakeMyBestLapPreferencesRepository(
    initialVoiceType: MyBestLapVoiceType = MyBestLapVoiceType.FORMAL,
) : MyBestLapPreferencesRepository {
    private val voiceType = MutableStateFlow(initialVoiceType)

    override fun observeVoiceType(): Flow<MyBestLapVoiceType> = voiceType

    override suspend fun saveVoiceType(type: MyBestLapVoiceType) {
        voiceType.update { type }
    }
}
