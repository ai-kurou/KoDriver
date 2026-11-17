package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.ManualReadoutItemRequest
import kurou.kodriver.domain.repository.ManualReadoutItemRequestRepository

public class RequestManualReadoutItemUseCase(
    private val repository: ManualReadoutItemRequestRepository,
) {
    public suspend operator fun invoke(request: ManualReadoutItemRequest): Unit = repository.request(request)
}
