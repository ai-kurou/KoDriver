package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.ManualReadoutRequest
import kurou.kodriver.domain.repository.ManualReadoutRequestRepository

public class RequestManualReadoutUseCase(
    private val repository: ManualReadoutRequestRepository,
) {
    public suspend operator fun invoke(request: ManualReadoutRequest): Unit = repository.request(request)
}
