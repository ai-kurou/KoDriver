package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.exportimport.Exportable
import kurou.kodriver.domain.model.VOICE_PITCH_MAX
import kurou.kodriver.domain.model.VOICE_PITCH_MIN
import kurou.kodriver.domain.repository.VoicePitchPreferencesRepository

internal class VoicePitchPreferencesRepositoryImpl(
    private val dataStore: DataStore<VoicePitchPreferences>,
) : VoicePitchPreferencesRepository,
    Exportable by DataStoreExportable(
        key = "voice_pitch",
        dataStore = dataStore,
        serializer = VoicePitchPreferences.serializer(),
        sanitize = { it.copy(voicePitch = it.voicePitch.coerceIn(VOICE_PITCH_MIN, VOICE_PITCH_MAX)) },
    ) {
    override fun voicePitch(): Flow<Float> = dataStore.observeProperty { it.voicePitch }

    override suspend fun saveVoicePitch(voicePitch: Float) {
        dataStore.saveProperty(voicePitch) { prefs, value -> prefs.copy(voicePitch = value) }
    }
}
