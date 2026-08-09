package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.core.model.RedFlagVoiceType
import kurou.kodriver.data.model.RedFlagPreferences
import kurou.kodriver.domain.model.RED_FLAG_VOICE_TYPE_DEFAULT
import kurou.kodriver.domain.repository.LmuWindowsRedFlagPreferencesRepository

internal class LmuWindowsRedFlagPreferencesRepositoryImpl(
    private val dataStore: DataStore<RedFlagPreferences>,
) : LmuWindowsRedFlagPreferencesRepository {
    override fun observeVoiceType(): Flow<RedFlagVoiceType> =
        dataStore.data.map { RedFlagVoiceType.fromId(it.voiceType) ?: RED_FLAG_VOICE_TYPE_DEFAULT }

    override suspend fun saveVoiceType(type: RedFlagVoiceType) {
        dataStore.updateData { it.copy(voiceType = type.id) }
    }
}
