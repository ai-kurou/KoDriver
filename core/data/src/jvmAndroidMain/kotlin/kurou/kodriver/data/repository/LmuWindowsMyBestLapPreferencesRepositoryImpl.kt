package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.data.model.MyBestLapPreferences
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapPreferencesRepository

internal class LmuWindowsMyBestLapPreferencesRepositoryImpl(
    private val dataStore: DataStore<MyBestLapPreferences>,
) : LmuWindowsMyBestLapPreferencesRepository {
    override fun observeVoiceType(): Flow<MyBestLapVoiceType> =
        dataStore.data.map { MyBestLapVoiceType.fromId(it.voiceType) }

    override suspend fun saveVoiceType(type: MyBestLapVoiceType) {
        dataStore.updateData { it.copy(voiceType = type.id) }
    }
}
