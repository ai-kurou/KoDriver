package kurou.kodriver.domain.speechrecognition.usecase

import kurou.kodriver.domain.speechrecognition.repository.WakeWordHotkeyRepository

public class RetryWakeWordHotkeyRegistrationUseCase(
    private val repository: WakeWordHotkeyRepository,
) {
    public suspend operator fun invoke(): Unit = repository.retryRegistration()
}
