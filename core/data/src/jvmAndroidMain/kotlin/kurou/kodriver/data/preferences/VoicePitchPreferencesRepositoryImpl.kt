package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.VoicePitchPreferencesRepository

internal class VoicePitchPreferencesRepositoryImpl(
    private val dataStore: DataStore<VoicePitchPreferences>,
) : VoicePitchPreferencesRepository {
    override fun voicePitch(): Flow<Float> = dataStore.observeProperty { it.voicePitch }

    override suspend fun saveVoicePitch(voicePitch: Float) {
        dataStore.saveProperty(voicePitch) { prefs, value -> prefs.copy(voicePitch = value) }
    }
}
