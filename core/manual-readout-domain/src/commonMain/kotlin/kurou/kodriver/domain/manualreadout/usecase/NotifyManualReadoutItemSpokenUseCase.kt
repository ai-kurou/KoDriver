package kurou.kodriver.domain.manualreadout.usecase

import kurou.kodriver.domain.manualreadout.model.ManualReadoutItemRequest
import kurou.kodriver.domain.manualreadout.repository.ManualReadoutItemSpokenRepository

public class NotifyManualReadoutItemSpokenUseCase(
    private val repository: ManualReadoutItemSpokenRepository,
) {
    public suspend operator fun invoke(request: ManualReadoutItemRequest): Unit = repository.notifySpoken(request)
}
