package kurou.kodriver.domain.usecase

import kurou.kodriver.core.model.ReadoutStartSoundType
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository

class SaveReadoutStartSoundTypeUseCase(
    private val repository: ReadoutStartSoundPreferencesRepository,
) {
    suspend operator fun invoke(type: ReadoutStartSoundType) = repository.saveType(type)
}
