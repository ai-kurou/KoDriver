package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.FlagPreferencesRepository

class SaveFlagEnabledStateUseCase(private val repository: FlagPreferencesRepository) {
    suspend operator fun invoke(key: String, enabled: Boolean) =
        repository.saveFlagEnabledState(key, enabled)
}
