package kurou.kodriver.domain.manualreadout.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.manualreadout.repository.ManualReadoutHotkeyRepository
import kurou.kodriver.domain.model.HotkeyRegistrationState

public class ObserveManualReadoutHotkeyRegistrationStateUseCase(
    private val repository: ManualReadoutHotkeyRepository,
) {
    public operator fun invoke(
        simulatorId: String,
        setId: String,
    ): Flow<HotkeyRegistrationState> = repository.registrationStateStream(simulatorId, setId)
}
