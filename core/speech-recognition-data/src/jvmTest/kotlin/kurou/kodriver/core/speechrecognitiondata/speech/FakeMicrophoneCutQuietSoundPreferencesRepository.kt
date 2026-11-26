package kurou.kodriver.core.speechrecognitiondata.speech

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kurou.kodriver.domain.microphone.model.MICROPHONE_CUT_QUIET_SOUND_LEVEL_DEFAULT
import kurou.kodriver.domain.microphone.repository.MicrophoneCutQuietSoundPreferencesRepository

internal class FakeMicrophoneCutQuietSoundPreferencesRepository(
    level: Int = MICROPHONE_CUT_QUIET_SOUND_LEVEL_DEFAULT,
) : MicrophoneCutQuietSoundPreferencesRepository {
    val level = MutableStateFlow(level)

    override fun observeMicrophoneCutQuietSoundLevel(): Flow<Int> = level

    override suspend fun saveMicrophoneCutQuietSoundLevel(level: Int) {
        this.level.value = level
    }

    val cutInCommand = MutableStateFlow(false)

    override fun observeMicrophoneCutQuietSoundInCommand(): Flow<Boolean> = cutInCommand

    override suspend fun saveMicrophoneCutQuietSoundInCommand(enabled: Boolean) {
        cutInCommand.value = enabled
    }
}
