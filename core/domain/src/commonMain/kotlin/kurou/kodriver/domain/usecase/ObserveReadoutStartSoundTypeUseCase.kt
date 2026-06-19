package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.repository.ReadoutStartSoundRepository

class ObserveReadoutStartSoundTypeUseCase(private val repository: ReadoutStartSoundRepository) {
    operator fun invoke(): Flow<ReadoutStartSoundType> = repository.observeType()
}
