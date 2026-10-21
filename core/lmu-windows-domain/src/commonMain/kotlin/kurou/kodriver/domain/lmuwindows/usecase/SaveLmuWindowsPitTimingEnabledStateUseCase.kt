package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitTimingPreferencesRepository
import kurou.kodriver.domain.model.ReadoutItemKey

class SaveLmuWindowsPitTimingEnabledStateUseCase(
    private val repository: LmuWindowsPitTimingPreferencesRepository,
) {
    suspend operator fun invoke(
        key: ReadoutItemKey,
        enabled: Boolean,
    ) = repository.saveEnabledState(key, enabled)
}
