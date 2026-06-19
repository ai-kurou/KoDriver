package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository

class SaveReadoutEnabledStateUseCase(private val repository: ReadoutPreferencesRepository) {
    suspend operator fun invoke(simulator: String, key: ReadoutItemKey, enabled: Boolean) =
        repository.saveReadoutEnabledState(simulator, key, enabled)
}
