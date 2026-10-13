package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.AutoReadoutPreferencesRepository

class SaveAutoReadoutEnabledStateUseCase(
    private val repository: AutoReadoutPreferencesRepository,
) {
    suspend operator fun invoke(
        simulator: String,
        key: ReadoutItemKey,
        enabled: Boolean,
    ) = repository.saveAutoReadoutEnabledState(simulator, key, enabled)
}
