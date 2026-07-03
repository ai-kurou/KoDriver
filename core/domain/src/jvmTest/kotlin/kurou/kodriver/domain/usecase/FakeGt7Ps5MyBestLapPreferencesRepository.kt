package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.repository.Gt7Ps5MyBestLapPreferencesRepository

class FakeGt7Ps5MyBestLapPreferencesRepository(
    initialVoiceType: MyBestLapVoiceType = MyBestLapVoiceType.FORMAL,
) : Gt7Ps5MyBestLapPreferencesRepository {
    private val voiceType = MutableStateFlow(initialVoiceType)

    override fun observeVoiceType(): Flow<MyBestLapVoiceType> = voiceType

    override suspend fun saveVoiceType(type: MyBestLapVoiceType) {
        voiceType.update { type }
    }
}
