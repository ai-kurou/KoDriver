package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ManualReadoutHotkeyRepository

public class RetryManualReadoutHotkeyRegistrationUseCase(
    private val repository: ManualReadoutHotkeyRepository,
) {
    public suspend operator fun invoke(
        simulatorId: String,
        setId: String,
    ): Unit = repository.retryRegistration(simulatorId, setId)
}
