package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.ReadoutStartSoundEnabledPreferencesRepository

public class SaveReadoutStartSoundEnabledStateUseCase(
    private val repository: ReadoutStartSoundEnabledPreferencesRepository,
) {
    public suspend operator fun invoke(
        key: ReadoutItemKey,
        enabled: Boolean,
    ): Unit = repository.saveStartSoundEnabledState(key, enabled)
}
