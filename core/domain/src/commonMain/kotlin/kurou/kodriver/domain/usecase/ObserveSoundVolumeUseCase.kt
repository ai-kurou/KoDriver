package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository

public class ObserveSoundVolumeUseCase(
    private val repository: SoundVolumePreferencesRepository,
) {
    public operator fun invoke(): Flow<Int> = repository.volume()
}
