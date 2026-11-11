package kurou.kodriver.data.manualreadout

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kurou.kodriver.domain.model.ManualReadoutItemRequest
import kurou.kodriver.domain.repository.ManualReadoutItemSpokenRepository

/**
 * 読み上げられた項目単位の依頼を、購読中の画面へ配信する。購読者がいなければ捨てる（音声タブを開いていない間は何も起きない）。
 * 画面が滞っても Narrator を待たせないよう、バッファが満杯なら古いものを捨てる。
 */
class InMemoryManualReadoutItemSpokenRepository : ManualReadoutItemSpokenRepository {
    private val spoken =
        MutableSharedFlow<ManualReadoutItemRequest>(
            extraBufferCapacity = 8,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    override fun observeSpoken(): Flow<ManualReadoutItemRequest> = spoken.asSharedFlow()

    override suspend fun notifySpoken(request: ManualReadoutItemRequest) {
        spoken.emit(request)
    }
}
