package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.RedFlagVoiceType
import kurou.kodriver.domain.repository.LmuWindowsRedFlagPreferencesRepository

internal class LmuWindowsRedFlagPreferencesRepositoryImpl(
    private val dataStore: DataStore<RedFlagPreferences>,
) : LmuWindowsRedFlagPreferencesRepository {
    override fun observeVoiceType(): Flow<RedFlagVoiceType> =
        dataStore.observeProperty { RedFlagVoiceType.fromId(it.voiceType) }

    override suspend fun saveVoiceType(type: RedFlagVoiceType) {
        dataStore.saveProperty(type.id) { prefs, value -> prefs.copy(voiceType = value) }
    }
}
