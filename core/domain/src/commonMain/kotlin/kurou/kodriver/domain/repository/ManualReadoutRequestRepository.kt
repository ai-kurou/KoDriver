package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ManualReadoutRequest

/** アプリ内で手動読み上げの依頼を配信する。 */
interface ManualReadoutRequestRepository {
    fun observeRequests(): Flow<ManualReadoutRequest>

    suspend fun request(request: ManualReadoutRequest)
}
