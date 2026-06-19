package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.FlagPreferencesRepository

class SaveFlagEnabledStateUseCase(private val repository: FlagPreferencesRepository) {
    suspend operator fun invoke(key: ReadoutItemKey, enabled: Boolean) =
        repository.saveFlagEnabledState(key, enabled)
}
