package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ManualReadoutItemRequest

/** アプリ内で手動読み上げの項目単位の依頼を配信する。 */
interface ManualReadoutItemRequestRepository {
    fun observeRequests(): Flow<ManualReadoutItemRequest>

    suspend fun request(request: ManualReadoutItemRequest)
}
