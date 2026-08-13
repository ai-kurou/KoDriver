package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapPreferencesRepository

internal class LmuWindowsMyBestLapPreferencesRepositoryImpl(
    private val dataStore: DataStore<MyBestLapPreferences>,
) : LmuWindowsMyBestLapPreferencesRepository {
    override fun observeVoiceType(): Flow<MyBestLapVoiceType> =
        dataStore.observeProperty { MyBestLapVoiceType.fromId(it.voiceType) }

    override suspend fun saveVoiceType(type: MyBestLapVoiceType) {
        dataStore.saveProperty(type.id) { prefs, value -> prefs.copy(voiceType = value) }
    }
}
