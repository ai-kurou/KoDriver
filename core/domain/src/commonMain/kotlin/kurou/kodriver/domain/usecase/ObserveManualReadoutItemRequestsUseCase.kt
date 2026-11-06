package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ManualReadoutItemRequest
import kurou.kodriver.domain.repository.ManualReadoutItemRequestRepository

class ObserveManualReadoutItemRequestsUseCase(
    private val repository: ManualReadoutItemRequestRepository,
) {
    operator fun invoke(): Flow<ManualReadoutItemRequest> = repository.observeRequests()
}
