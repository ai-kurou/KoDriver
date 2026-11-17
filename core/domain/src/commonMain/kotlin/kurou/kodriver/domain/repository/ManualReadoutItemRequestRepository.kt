package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ManualReadoutItemRequest

/** アプリ内で手動読み上げの項目単位の依頼を配信する。 */
public interface ManualReadoutItemRequestRepository {
    public fun observeRequests(): Flow<ManualReadoutItemRequest>

    public suspend fun request(request: ManualReadoutItemRequest)
}
