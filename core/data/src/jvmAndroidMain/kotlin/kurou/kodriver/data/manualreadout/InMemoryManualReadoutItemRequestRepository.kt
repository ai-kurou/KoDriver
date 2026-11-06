package kurou.kodriver.data.manualreadout

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kurou.kodriver.domain.model.ManualReadoutItemRequest
import kurou.kodriver.domain.repository.ManualReadoutItemRequestRepository

/**
 * 購読中の Narrator に項目単位の依頼を配信する。購読開始前の依頼は再配信しない。
 * Narrator が滞っても音声認識の購読側を待たせないよう、バッファが満杯なら古い依頼を捨てる。
 */
class InMemoryManualReadoutItemRequestRepository : ManualReadoutItemRequestRepository {
    private val requests =
        MutableSharedFlow<ManualReadoutItemRequest>(
            extraBufferCapacity = 64,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    override fun observeRequests(): Flow<ManualReadoutItemRequest> = requests.asSharedFlow()

    override suspend fun request(request: ManualReadoutItemRequest) {
        requests.emit(request)
    }
}
