package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.repository.ReadoutStartSoundRepository

class SaveReadoutStartSoundTypeUseCase(private val repository: ReadoutStartSoundRepository) {
    suspend operator fun invoke(type: ReadoutStartSoundType) = repository.saveType(type)
}
