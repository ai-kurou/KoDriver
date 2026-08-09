package kurou.kodriver.domain.usecase

import kurou.kodriver.core.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsFlagPreferencesRepository

class SaveLmuWindowsFlagEnabledStateUseCase(
    private val repository: LmuWindowsFlagPreferencesRepository,
) {
    suspend operator fun invoke(
        key: ReadoutItemKey,
        enabled: Boolean,
    ) = repository.saveFlagEnabledState(key, enabled)
}
