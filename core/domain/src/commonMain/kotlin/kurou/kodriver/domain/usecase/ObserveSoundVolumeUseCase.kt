package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.SoundVolumeRepository

class ObserveSoundVolumeUseCase(private val repository: SoundVolumeRepository) {
    operator fun invoke(): Flow<Int> = repository.volume()
}
