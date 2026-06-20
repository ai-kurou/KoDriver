package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository

class ObserveReadoutStartSoundTypeUseCase(private val repository: ReadoutStartSoundPreferencesRepository) {
    operator fun invoke(): Flow<ReadoutStartSoundType> = repository.observeType()
}
