package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.VoicePreferencesRepository

internal class VoicePreferencesRepositoryImpl(
    private val dataStore: DataStore<VoicePreferences>,
) : VoicePreferencesRepository {
    override fun voiceId(): Flow<String> = dataStore.observeProperty { it.voiceId }

    override suspend fun saveVoiceId(voiceId: String) {
        dataStore.saveProperty(voiceId) { prefs, value -> prefs.copy(voiceId = value) }
    }
}
