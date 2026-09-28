package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsPitTimingPreferencesRepository

class SaveLmuWindowsPitTimingEnabledStateUseCase(
    private val repository: LmuWindowsPitTimingPreferencesRepository,
) {
    suspend operator fun invoke(
        key: ReadoutItemKey,
        enabled: Boolean,
    ) = repository.saveEnabledState(key, enabled)
}
