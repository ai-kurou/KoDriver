package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.repository.ManualReadoutHotkeyRepository

class ObserveManualReadoutHotkeyRegistrationStateUseCase(
    private val repository: ManualReadoutHotkeyRepository,
) {
    operator fun invoke(
        simulatorId: String,
        setId: String,
    ): Flow<HotkeyRegistrationState> = repository.registrationStateStream(simulatorId, setId)
}
