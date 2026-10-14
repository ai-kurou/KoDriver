package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ManualReadoutHotkeyRepository

class RetryManualReadoutHotkeyRegistrationUseCase(
    private val repository: ManualReadoutHotkeyRepository,
) {
    suspend operator fun invoke(
        simulatorId: String,
        setId: String,
    ) = repository.retryRegistration(simulatorId, setId)
}
