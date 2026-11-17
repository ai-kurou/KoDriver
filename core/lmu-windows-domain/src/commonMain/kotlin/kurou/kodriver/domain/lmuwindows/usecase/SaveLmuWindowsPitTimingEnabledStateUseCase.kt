package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitTimingPreferencesRepository
import kurou.kodriver.domain.model.ReadoutItemKey

public class SaveLmuWindowsPitTimingEnabledStateUseCase(
    private val repository: LmuWindowsPitTimingPreferencesRepository,
) {
    public suspend operator fun invoke(
        key: ReadoutItemKey,
        enabled: Boolean,
    ): Unit = repository.saveEnabledState(key, enabled)
}
