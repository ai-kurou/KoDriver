package kurou.kodriver.domain.manualreadout.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.manualreadout.model.ManualReadoutItemRequest
import kurou.kodriver.domain.manualreadout.repository.ManualReadoutItemRequestRepository

public class ObserveManualReadoutItemRequestsUseCase(
    private val repository: ManualReadoutItemRequestRepository,
) {
    public operator fun invoke(): Flow<ManualReadoutItemRequest> = repository.observeRequests()
}
