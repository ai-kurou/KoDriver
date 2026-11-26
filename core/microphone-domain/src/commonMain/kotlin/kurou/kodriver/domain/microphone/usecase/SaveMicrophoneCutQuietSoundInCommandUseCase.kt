package kurou.kodriver.domain.microphone.usecase

import kurou.kodriver.domain.microphone.repository.MicrophoneCutQuietSoundPreferencesRepository

public class SaveMicrophoneCutQuietSoundInCommandUseCase(
    private val repository: MicrophoneCutQuietSoundPreferencesRepository,
) {
    public suspend operator fun invoke(enabled: Boolean) {
        repository.saveMicrophoneCutQuietSoundInCommand(enabled)
    }
}
