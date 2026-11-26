package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ManualReadoutRequest
import kurou.kodriver.domain.repository.ManualReadoutHotkeyRepository

public class ObserveManualReadoutHotkeyPressesUseCase(
    private val repository: ManualReadoutHotkeyRepository,
) {
    public operator fun invoke(): Flow<ManualReadoutRequest> = repository.pressStream()
}
