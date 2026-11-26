package kurou.kodriver.domain.microphone.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.microphone.repository.MicrophoneCutQuietSoundPreferencesRepository

public class ObserveMicrophoneCutQuietSoundInCommandUseCase(
    private val repository: MicrophoneCutQuietSoundPreferencesRepository,
) {
    public operator fun invoke(): Flow<Boolean> = repository.observeMicrophoneCutQuietSoundInCommand()
}
