package kurou.kodriver.domain.manualreadout.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.manualreadout.model.ManualReadoutItemRequest

/** アプリ内で手動読み上げの項目単位の依頼を配信する。 */
public interface ManualReadoutItemRequestRepository {
    public fun observeRequests(): Flow<ManualReadoutItemRequest>

    public suspend fun request(request: ManualReadoutItemRequest)
}
