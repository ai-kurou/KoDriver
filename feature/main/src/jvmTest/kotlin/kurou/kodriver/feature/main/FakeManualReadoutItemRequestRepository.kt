package kurou.kodriver.feature.main

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.domain.model.ManualReadoutItemRequest
import kurou.kodriver.domain.repository.ManualReadoutItemRequestRepository

/** 発行された項目依頼を記録するだけのテスト用Repository。 */
internal class FakeManualReadoutItemRequestRepository : ManualReadoutItemRequestRepository {
    val requests = mutableListOf<ManualReadoutItemRequest>()

    /** 設定すると、依頼のたびにその例外を投げる。 */
    var failure: Exception? = null

    override fun observeRequests(): Flow<ManualReadoutItemRequest> = emptyFlow()

    override suspend fun request(request: ManualReadoutItemRequest) {
        failure?.let { throw it }
        requests.add(request)
    }
}
