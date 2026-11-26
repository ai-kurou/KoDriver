package kurou.kodriver.domain.manualreadout.usecase

import kurou.kodriver.domain.manualreadout.repository.ManualReadoutHotkeyRepository

public class RetryManualReadoutHotkeyRegistrationUseCase(
    private val repository: ManualReadoutHotkeyRepository,
) {
    public suspend operator fun invoke(
        simulatorId: String,
        setId: String,
    ): Unit = repository.retryRegistration(simulatorId, setId)
}
