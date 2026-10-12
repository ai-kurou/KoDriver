package kurou.kodriver.data.debugreadout

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kurou.kodriver.domain.model.DebugReadoutRequest
import kurou.kodriver.domain.repository.DebugReadoutRequestRepository

class InMemoryDebugReadoutRequestRepository : DebugReadoutRequestRepository {
    private val requests = MutableSharedFlow<DebugReadoutRequest>()

    override fun observeRequests(): Flow<DebugReadoutRequest> = requests.asSharedFlow()

    override suspend fun requestReadout(request: DebugReadoutRequest) {
        requests.emit(request)
    }
}
