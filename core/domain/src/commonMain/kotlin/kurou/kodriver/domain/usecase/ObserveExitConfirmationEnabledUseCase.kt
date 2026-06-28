package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ExitConfirmationPreferencesRepository

class ObserveExitConfirmationEnabledUseCase(private val repository: ExitConfirmationPreferencesRepository) {
    operator fun invoke() = repository.exitConfirmationEnabled()
}
