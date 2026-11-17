package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ManualReadoutRequest
import kurou.kodriver.domain.repository.ManualReadoutRequestRepository

public class ObserveManualReadoutRequestsUseCase(
    private val repository: ManualReadoutRequestRepository,
) {
    public operator fun invoke(): Flow<ManualReadoutRequest> = repository.observeRequests()
}
