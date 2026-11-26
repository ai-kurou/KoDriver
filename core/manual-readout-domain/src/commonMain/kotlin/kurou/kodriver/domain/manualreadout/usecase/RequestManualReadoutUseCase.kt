package kurou.kodriver.domain.manualreadout.usecase

import kurou.kodriver.domain.manualreadout.model.ManualReadoutRequest
import kurou.kodriver.domain.manualreadout.repository.ManualReadoutRequestRepository

public class RequestManualReadoutUseCase(
    private val repository: ManualReadoutRequestRepository,
) {
    public suspend operator fun invoke(request: ManualReadoutRequest): Unit = repository.request(request)
}
