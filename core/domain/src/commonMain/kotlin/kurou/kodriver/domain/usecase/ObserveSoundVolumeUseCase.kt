package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository

class ObserveSoundVolumeUseCase(private val repository: SoundVolumePreferencesRepository) {
    operator fun invoke(): Flow<Int> = repository.volume()
}
