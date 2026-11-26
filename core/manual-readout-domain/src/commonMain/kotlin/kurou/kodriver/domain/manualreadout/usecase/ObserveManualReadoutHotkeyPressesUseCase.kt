package kurou.kodriver.domain.manualreadout.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.manualreadout.model.ManualReadoutRequest
import kurou.kodriver.domain.manualreadout.repository.ManualReadoutHotkeyRepository

public class ObserveManualReadoutHotkeyPressesUseCase(
    private val repository: ManualReadoutHotkeyRepository,
) {
    public operator fun invoke(): Flow<ManualReadoutRequest> = repository.pressStream()
}
