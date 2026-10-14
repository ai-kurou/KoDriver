package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ManualReadoutRequest
import kurou.kodriver.domain.repository.ManualReadoutRequestRepository

class ObserveManualReadoutRequestsUseCase(
    private val repository: ManualReadoutRequestRepository,
) {
    operator fun invoke(): Flow<ManualReadoutRequest> = repository.observeRequests()
}
