package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.ManualReadoutItemRequest
import kurou.kodriver.domain.repository.ManualReadoutItemSpokenRepository

class NotifyManualReadoutItemSpokenUseCase(
    private val repository: ManualReadoutItemSpokenRepository,
) {
    suspend operator fun invoke(request: ManualReadoutItemRequest) = repository.notifySpoken(request)
}
