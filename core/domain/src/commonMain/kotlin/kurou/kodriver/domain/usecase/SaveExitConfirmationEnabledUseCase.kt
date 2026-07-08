package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ExitConfirmationEnabledRepository

class SaveExitConfirmationEnabledUseCase(private val repository: ExitConfirmationEnabledRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.saveExitConfirmationEnabled(enabled)
}
