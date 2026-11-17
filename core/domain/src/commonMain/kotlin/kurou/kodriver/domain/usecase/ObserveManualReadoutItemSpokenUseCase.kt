package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ManualReadoutItemRequest
import kurou.kodriver.domain.repository.ManualReadoutItemSpokenRepository

public class ObserveManualReadoutItemSpokenUseCase(
    private val repository: ManualReadoutItemSpokenRepository,
) {
    public operator fun invoke(): Flow<ManualReadoutItemRequest> = repository.observeSpoken()
}
