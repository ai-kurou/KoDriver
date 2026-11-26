package kurou.kodriver.domain.debug.usecase

import kurou.kodriver.domain.debug.model.DebugReadoutRequest
import kurou.kodriver.domain.debug.repository.DebugReadoutRequestRepository

public class RequestDebugReadoutUseCase(
    private val repository: DebugReadoutRequestRepository,
) {
    public suspend operator fun invoke(request: DebugReadoutRequest): Unit = repository.requestReadout(request)
}
