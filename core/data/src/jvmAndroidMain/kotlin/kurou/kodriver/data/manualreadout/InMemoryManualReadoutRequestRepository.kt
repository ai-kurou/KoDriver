package kurou.kodriver.data.manualreadout

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kurou.kodriver.domain.model.ManualReadoutRequest
import kurou.kodriver.domain.repository.ManualReadoutRequestRepository

/** 購読中の Narrator に依頼を配信する。購読開始前の依頼は再配信しない。 */
class InMemoryManualReadoutRequestRepository : ManualReadoutRequestRepository {
    // 連続タップはバッファに保持し、満杯なら emit が待機して依頼を取りこぼさない。
    private val requests = MutableSharedFlow<ManualReadoutRequest>(extraBufferCapacity = 64)

    override fun observeRequests(): Flow<ManualReadoutRequest> = requests.asSharedFlow()

    override suspend fun request(request: ManualReadoutRequest) {
        requests.emit(request)
    }
}
