package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ManualReadoutItemRequest
import kurou.kodriver.domain.repository.ManualReadoutItemSpokenRepository

class ObserveManualReadoutItemSpokenUseCase(
    private val repository: ManualReadoutItemSpokenRepository,
) {
    operator fun invoke(): Flow<ManualReadoutItemRequest> = repository.observeSpoken()
}
