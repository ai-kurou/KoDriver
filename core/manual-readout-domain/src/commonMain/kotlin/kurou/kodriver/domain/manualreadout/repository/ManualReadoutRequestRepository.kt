package kurou.kodriver.domain.manualreadout.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.manualreadout.model.ManualReadoutRequest

/** アプリ内で手動読み上げの依頼を配信する。 */
public interface ManualReadoutRequestRepository {
    public fun observeRequests(): Flow<ManualReadoutRequest>

    public suspend fun request(request: ManualReadoutRequest)
}
