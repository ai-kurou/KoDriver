package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.DebugReadoutRequest

/** デバッグ要求をプロセス内で配信する。購読開始前の要求は再配信しない。 */
public interface DebugReadoutRequestRepository {
    public fun observeRequests(): Flow<DebugReadoutRequest>

    public suspend fun requestReadout(request: DebugReadoutRequest)
}
