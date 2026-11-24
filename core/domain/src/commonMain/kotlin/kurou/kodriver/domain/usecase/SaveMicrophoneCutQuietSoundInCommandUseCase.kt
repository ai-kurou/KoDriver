package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.MicrophoneCutQuietSoundPreferencesRepository

public class SaveMicrophoneCutQuietSoundInCommandUseCase(
    private val repository: MicrophoneCutQuietSoundPreferencesRepository,
) {
    public suspend operator fun invoke(enabled: Boolean) {
        repository.saveMicrophoneCutQuietSoundInCommand(enabled)
    }
}
