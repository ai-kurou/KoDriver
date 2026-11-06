package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.ManualReadoutItemRequest
import kurou.kodriver.domain.repository.ManualReadoutItemRequestRepository

class RequestManualReadoutItemUseCase(
    private val repository: ManualReadoutItemRequestRepository,
) {
    suspend operator fun invoke(request: ManualReadoutItemRequest) = repository.request(request)
}
