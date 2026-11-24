package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.exportimport.Exportable
import kurou.kodriver.domain.model.VOICE_SPEED_MAX
import kurou.kodriver.domain.model.VOICE_SPEED_MIN
import kurou.kodriver.domain.repository.VoiceSpeedPreferencesRepository

internal class VoiceSpeedPreferencesRepositoryImpl(
    private val dataStore: DataStore<VoiceSpeedPreferences>,
) : VoiceSpeedPreferencesRepository,
    Exportable by DataStoreExportable(
        key = "voice_speed",
        dataStore = dataStore,
        serializer = VoiceSpeedPreferences.serializer(),
        sanitize = { it.copy(voiceSpeed = it.voiceSpeed.coerceIn(VOICE_SPEED_MIN, VOICE_SPEED_MAX)) },
    ) {
    override fun voiceSpeed(): Flow<Float> = dataStore.observeProperty { it.voiceSpeed }

    override suspend fun saveVoiceSpeed(voiceSpeed: Float) {
        dataStore.saveProperty(voiceSpeed) { prefs, value -> prefs.copy(voiceSpeed = value) }
    }
}
