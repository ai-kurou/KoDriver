package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ManualReadoutItemRequest
import kurou.kodriver.domain.repository.ManualReadoutItemRequestRepository

public class ObserveManualReadoutItemRequestsUseCase(
    private val repository: ManualReadoutItemRequestRepository,
) {
    public operator fun invoke(): Flow<ManualReadoutItemRequest> = repository.observeRequests()
}
