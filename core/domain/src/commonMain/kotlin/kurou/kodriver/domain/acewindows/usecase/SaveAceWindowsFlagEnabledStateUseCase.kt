package kurou.kodriver.domain.acewindows.usecase

import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagPreferencesRepository
import kurou.kodriver.domain.model.ReadoutItemKey

class SaveAceWindowsFlagEnabledStateUseCase(
    private val repository: AceWindowsFlagPreferencesRepository,
) {
    suspend operator fun invoke(
        key: ReadoutItemKey,
        enabled: Boolean,
    ) = repository.saveFlagEnabledState(key, enabled)
}
