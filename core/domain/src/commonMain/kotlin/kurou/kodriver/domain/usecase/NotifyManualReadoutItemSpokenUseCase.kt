package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.ManualReadoutItemRequest
import kurou.kodriver.domain.repository.ManualReadoutItemSpokenRepository

public class NotifyManualReadoutItemSpokenUseCase(
    private val repository: ManualReadoutItemSpokenRepository,
) {
    public suspend operator fun invoke(request: ManualReadoutItemRequest): Unit = repository.notifySpoken(request)
}
