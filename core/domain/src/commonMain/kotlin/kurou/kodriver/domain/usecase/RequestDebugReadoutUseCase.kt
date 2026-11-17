package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.DebugReadoutRequest
import kurou.kodriver.domain.repository.DebugReadoutRequestRepository

public class RequestDebugReadoutUseCase(
    private val repository: DebugReadoutRequestRepository,
) {
    public suspend operator fun invoke(request: DebugReadoutRequest): Unit = repository.requestReadout(request)
}
