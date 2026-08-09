package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.core.model.MyBestLapVoiceType
import kurou.kodriver.data.model.MyBestLapPreferences
import kurou.kodriver.domain.model.MY_BEST_LAP_VOICE_TYPE_DEFAULT
import kurou.kodriver.domain.repository.Gt7Ps5MyBestLapPreferencesRepository

internal class Gt7Ps5MyBestLapPreferencesRepositoryImpl(
    private val dataStore: DataStore<MyBestLapPreferences>,
) : Gt7Ps5MyBestLapPreferencesRepository {
    override fun observeVoiceType(): Flow<MyBestLapVoiceType> =
        dataStore.data.map {
            MyBestLapVoiceType.fromId(it.voiceType) ?: MY_BEST_LAP_VOICE_TYPE_DEFAULT
        }

    override suspend fun saveVoiceType(type: MyBestLapVoiceType) {
        dataStore.updateData { it.copy(voiceType = type.id) }
    }
}
