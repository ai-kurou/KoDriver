package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsRainPreferencesRepository

class SaveLmuWindowsRainEnabledStateUseCase(
    private val repository: LmuWindowsRainPreferencesRepository,
) {
    suspend operator fun invoke(
        key: ReadoutItemKey,
        enabled: Boolean,
    ) = repository.saveRainEnabledState(key, enabled)
}
