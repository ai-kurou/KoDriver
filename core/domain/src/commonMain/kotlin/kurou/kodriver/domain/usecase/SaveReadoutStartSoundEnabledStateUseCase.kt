package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.ReadoutStartSoundEnabledPreferencesRepository

class SaveReadoutStartSoundEnabledStateUseCase(
    private val repository: ReadoutStartSoundEnabledPreferencesRepository,
) {
    suspend operator fun invoke(
        key: ReadoutItemKey,
        enabled: Boolean,
    ) = repository.saveStartSoundEnabledState(key, enabled)
}
