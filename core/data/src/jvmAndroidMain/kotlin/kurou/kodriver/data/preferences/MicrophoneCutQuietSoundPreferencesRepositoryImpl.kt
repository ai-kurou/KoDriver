package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.exportimport.Exportable
import kurou.kodriver.domain.model.MICROPHONE_CUT_QUIET_SOUND_LEVEL_MAX
import kurou.kodriver.domain.model.MICROPHONE_CUT_QUIET_SOUND_LEVEL_MIN
import kurou.kodriver.domain.repository.MicrophoneCutQuietSoundPreferencesRepository

internal class MicrophoneCutQuietSoundPreferencesRepositoryImpl(
    private val dataStore: DataStore<MicrophoneCutQuietSoundPreferences>,
) : MicrophoneCutQuietSoundPreferencesRepository,
    Exportable by DataStoreExportable(
        key = "microphone_cut_quiet_sound",
        dataStore = dataStore,
        serializer = MicrophoneCutQuietSoundPreferences.serializer(),
        sanitize = {
            it.copy(
                level = it.level.coerceIn(MICROPHONE_CUT_QUIET_SOUND_LEVEL_MIN, MICROPHONE_CUT_QUIET_SOUND_LEVEL_MAX),
            )
        },
    ) {
    override fun observeMicrophoneCutQuietSoundLevel(): Flow<Int> = dataStore.observeProperty { it.level }

    override suspend fun saveMicrophoneCutQuietSoundLevel(level: Int) {
        dataStore.saveProperty(level) { prefs, value -> prefs.copy(level = value) }
    }
}
