package kurou.kodriver.domain.manualreadout.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.manualreadout.model.ManualReadoutItemRequest
import kurou.kodriver.domain.manualreadout.repository.ManualReadoutItemSpokenRepository

public class ObserveManualReadoutItemSpokenUseCase(
    private val repository: ManualReadoutItemSpokenRepository,
) {
    public operator fun invoke(): Flow<ManualReadoutItemRequest> = repository.observeSpoken()
}
