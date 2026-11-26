package kurou.kodriver.domain.debug.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.debug.model.DebugReadoutRequest
import kurou.kodriver.domain.debug.repository.DebugReadoutRequestRepository

public class ObserveDebugReadoutRequestUseCase(
    private val repository: DebugReadoutRequestRepository,
) {
    public operator fun invoke(): Flow<DebugReadoutRequest> = repository.observeRequests()
}
