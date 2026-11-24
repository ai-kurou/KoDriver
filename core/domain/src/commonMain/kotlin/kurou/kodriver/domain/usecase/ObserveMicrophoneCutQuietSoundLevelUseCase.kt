package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.MicrophoneCutQuietSoundPreferencesRepository

public class ObserveMicrophoneCutQuietSoundLevelUseCase(
    private val repository: MicrophoneCutQuietSoundPreferencesRepository,
) {
    public operator fun invoke(): Flow<Int> = repository.observeMicrophoneCutQuietSoundLevel()
}
