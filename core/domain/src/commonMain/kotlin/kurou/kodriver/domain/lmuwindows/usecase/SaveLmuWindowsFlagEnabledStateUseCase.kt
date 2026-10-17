package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsFlagPreferencesRepository
import kurou.kodriver.domain.model.ReadoutItemKey

class SaveLmuWindowsFlagEnabledStateUseCase(
    private val repository: LmuWindowsFlagPreferencesRepository,
) {
    suspend operator fun invoke(
        key: ReadoutItemKey,
        enabled: Boolean,
    ) = repository.saveFlagEnabledState(key, enabled)
}
