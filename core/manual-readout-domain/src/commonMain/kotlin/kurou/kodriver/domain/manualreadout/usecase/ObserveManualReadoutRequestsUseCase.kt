package kurou.kodriver.domain.manualreadout.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.manualreadout.model.ManualReadoutRequest
import kurou.kodriver.domain.manualreadout.repository.ManualReadoutRequestRepository

public class ObserveManualReadoutRequestsUseCase(
    private val repository: ManualReadoutRequestRepository,
) {
    public operator fun invoke(): Flow<ManualReadoutRequest> = repository.observeRequests()
}
