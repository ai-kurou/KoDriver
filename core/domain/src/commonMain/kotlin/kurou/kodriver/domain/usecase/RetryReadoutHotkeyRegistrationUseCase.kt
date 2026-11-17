package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.HotkeyRepository

public class RetryReadoutHotkeyRegistrationUseCase(
    private val repository: HotkeyRepository,
) {
    public suspend operator fun invoke(): Unit = repository.retryRegistration()
}
