package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.DebugReadoutRequest
import kurou.kodriver.domain.repository.DebugReadoutRequestRepository

class RequestDebugReadoutUseCase(
    private val repository: DebugReadoutRequestRepository,
) {
    suspend operator fun invoke(request: DebugReadoutRequest) = repository.requestReadout(request)
}
