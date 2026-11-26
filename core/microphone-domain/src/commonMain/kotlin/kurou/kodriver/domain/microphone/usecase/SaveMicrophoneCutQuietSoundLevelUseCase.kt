package kurou.kodriver.domain.microphone.usecase

import kurou.kodriver.domain.microphone.model.MICROPHONE_CUT_QUIET_SOUND_LEVEL_MAX
import kurou.kodriver.domain.microphone.model.MICROPHONE_CUT_QUIET_SOUND_LEVEL_MIN
import kurou.kodriver.domain.microphone.repository.MicrophoneCutQuietSoundPreferencesRepository

public class SaveMicrophoneCutQuietSoundLevelUseCase(
    private val repository: MicrophoneCutQuietSoundPreferencesRepository,
) {
    public suspend operator fun invoke(level: Int) {
        require(level in MICROPHONE_CUT_QUIET_SOUND_LEVEL_MIN..MICROPHONE_CUT_QUIET_SOUND_LEVEL_MAX) {
            "level must be between $MICROPHONE_CUT_QUIET_SOUND_LEVEL_MIN and $MICROPHONE_CUT_QUIET_SOUND_LEVEL_MAX"
        }
        repository.saveMicrophoneCutQuietSoundLevel(level)
    }
}
