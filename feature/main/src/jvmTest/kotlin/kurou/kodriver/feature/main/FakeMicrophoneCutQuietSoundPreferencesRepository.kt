package kurou.kodriver.feature.main

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kurou.kodriver.domain.microphone.model.MICROPHONE_CUT_QUIET_SOUND_LEVEL_MIN
import kurou.kodriver.domain.microphone.repository.MicrophoneCutQuietSoundPreferencesRepository

/** 保存した段階値をそのまま流すフェイク。初期値は「カットしない」で、UiState の既定値と一致する。 */
internal class FakeMicrophoneCutQuietSoundPreferencesRepository(
    initialLevel: Int = MICROPHONE_CUT_QUIET_SOUND_LEVEL_MIN,
) : MicrophoneCutQuietSoundPreferencesRepository {
    val level = MutableStateFlow(initialLevel)

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
