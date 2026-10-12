package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.DebugReadoutRequest
import kurou.kodriver.domain.repository.DebugReadoutRequestRepository

class ObserveDebugReadoutRequestUseCase(
    private val repository: DebugReadoutRequestRepository,
) {
    operator fun invoke(): Flow<DebugReadoutRequest> = repository.observeRequests()
}
