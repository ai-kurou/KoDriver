package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ExitConfirmationPreferencesRepository

class SaveExitConfirmationEnabledUseCase(private val repository: ExitConfirmationPreferencesRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.saveExitConfirmationEnabled(enabled)
}
