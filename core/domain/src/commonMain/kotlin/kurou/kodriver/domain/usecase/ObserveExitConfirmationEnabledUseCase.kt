package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ExitConfirmationEnabledRepository

class ObserveExitConfirmationEnabledUseCase(private val repository: ExitConfirmationEnabledRepository) {
    operator fun invoke() = repository.exitConfirmationEnabled()
}
