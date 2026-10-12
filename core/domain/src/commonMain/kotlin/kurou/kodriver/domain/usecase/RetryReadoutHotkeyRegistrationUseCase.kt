package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.HotkeyRepository

class RetryReadoutHotkeyRegistrationUseCase(
    private val repository: HotkeyRepository,
) {
    suspend operator fun invoke() = repository.retryRegistration()
}
