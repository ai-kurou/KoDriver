package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ReadoutPreferencesRepository

class SaveReadoutEnabledStateUseCase(private val repository: ReadoutPreferencesRepository) {
    suspend operator fun invoke(simulator: String, label: String, enabled: Boolean) =
        repository.saveReadoutEnabledState(simulator, label, enabled)
}
