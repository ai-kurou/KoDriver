package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.MICROPHONE_CUT_QUIET_SOUND_LEVEL_MAX
import kurou.kodriver.domain.model.MICROPHONE_CUT_QUIET_SOUND_LEVEL_MIN
import kurou.kodriver.domain.repository.MicrophoneCutQuietSoundPreferencesRepository

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
