package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.VoiceSpeedPreferencesRepository

internal class VoiceSpeedPreferencesRepositoryImpl(
    private val dataStore: DataStore<VoiceSpeedPreferences>,
) : VoiceSpeedPreferencesRepository {
    override fun voiceSpeed(): Flow<Float> = dataStore.observeProperty { it.voiceSpeed }

    override suspend fun saveVoiceSpeed(voiceSpeed: Float) {
        dataStore.saveProperty(voiceSpeed) { prefs, value -> prefs.copy(voiceSpeed = value) }
    }
}
