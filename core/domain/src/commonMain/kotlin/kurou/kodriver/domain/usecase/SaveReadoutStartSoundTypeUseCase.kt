package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository

public class SaveReadoutStartSoundTypeUseCase(
    private val repository: ReadoutStartSoundPreferencesRepository,
) {
    public suspend operator fun invoke(type: ReadoutStartSoundType): Unit = repository.saveType(type)
}
