package kurou.kodriver.domain.manualreadout.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.manualreadout.model.HotkeyRegistrationState
import kurou.kodriver.domain.manualreadout.repository.ManualReadoutHotkeyRepository

public class ObserveManualReadoutHotkeyRegistrationStateUseCase(
    private val repository: ManualReadoutHotkeyRepository,
) {
    public operator fun invoke(
        simulatorId: String,
        setId: String,
    ): Flow<HotkeyRegistrationState> = repository.registrationStateStream(simulatorId, setId)
}
