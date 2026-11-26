package kurou.kodriver.domain.manualreadout.usecase

import kurou.kodriver.domain.manualreadout.model.ManualReadoutItemRequest
import kurou.kodriver.domain.manualreadout.repository.ManualReadoutItemRequestRepository

public class RequestManualReadoutItemUseCase(
    private val repository: ManualReadoutItemRequestRepository,
) {
    public suspend operator fun invoke(request: ManualReadoutItemRequest): Unit = repository.request(request)
}
