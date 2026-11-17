package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsFlagPreferencesRepository
import kurou.kodriver.domain.model.ReadoutItemKey

public class SaveLmuWindowsFlagEnabledStateUseCase(
    private val repository: LmuWindowsFlagPreferencesRepository,
) {
    public suspend operator fun invoke(
        key: ReadoutItemKey,
        enabled: Boolean,
    ): Unit = repository.saveFlagEnabledState(key, enabled)
}
