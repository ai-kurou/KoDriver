package kurou.kodriver.feature.gt7ps5readout.mybestlapdetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.repository.MyBestLapPreferencesRepository

class FakeMyBestLapPreferencesRepository : MyBestLapPreferencesRepository {
    private val voiceType = MutableStateFlow(MyBestLapVoiceType.FORMAL)

    override fun observeVoiceType(): Flow<MyBestLapVoiceType> = voiceType

    override suspend fun saveVoiceType(type: MyBestLapVoiceType) {
        voiceType.update { type }
    }
}
