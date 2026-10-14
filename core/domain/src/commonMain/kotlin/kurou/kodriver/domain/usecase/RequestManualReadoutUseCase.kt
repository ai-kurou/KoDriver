package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.ManualReadoutRequest
import kurou.kodriver.domain.repository.ManualReadoutRequestRepository

class RequestManualReadoutUseCase(
    private val repository: ManualReadoutRequestRepository,
) {
    suspend operator fun invoke(request: ManualReadoutRequest) = repository.request(request)
}
