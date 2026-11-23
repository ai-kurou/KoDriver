package kurou.kodriver.core.speechrecognitiondata.speech

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kurou.kodriver.domain.model.MICROPHONE_CUT_QUIET_SOUND_LEVEL_DEFAULT
import kurou.kodriver.domain.repository.MicrophoneCutQuietSoundPreferencesRepository

internal class FakeMicrophoneCutQuietSoundPreferencesRepository(
    level: Int = MICROPHONE_CUT_QUIET_SOUND_LEVEL_DEFAULT,
) : MicrophoneCutQuietSoundPreferencesRepository {
    val level = MutableStateFlow(level)

    override fun observeMicrophoneCutQuietSoundLevel(): Flow<Int> = level

    override suspend fun saveMicrophoneCutQuietSoundLevel(level: Int) {
        this.level.value = level
    }
}
