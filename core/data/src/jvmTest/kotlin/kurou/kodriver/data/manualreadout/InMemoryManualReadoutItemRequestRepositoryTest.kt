package kurou.kodriver.data.manualreadout

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ManualReadoutItemRequest
import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertEquals

class InMemoryManualReadoutItemRequestRepositoryTest {
    @Test
    fun `連続した項目依頼を全購読者に順番通り配信し過去の依頼は再配信しない`() =
        runTest {
            val repository = InMemoryManualReadoutItemRequestRepository()
            repository.request(ManualReadoutItemRequest(Simulator.LmuWindows.id, "before"))
            val first = mutableListOf<ManualReadoutItemRequest>()
            val second = mutableListOf<ManualReadoutItemRequest>()
            backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
                repository.observeRequests().collect { first.add(it) }
            }
            backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
                repository.observeRequests().collect { second.add(it) }
            }
            // バッファ容量を超えても emit の待機により順序を保って配信する。
            val requests = (1..100).map { ManualReadoutItemRequest(Simulator.LmuWindows.id, "item-$it") }
            requests.forEach { repository.request(it) }
            testScheduler.runCurrent()
            assertEquals(requests, first)
            assertEquals(requests, second)
            val late = mutableListOf<ManualReadoutItemRequest>()
            backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
                repository.observeRequests().collect { late.add(it) }
            }
            testScheduler.runCurrent()
            assertEquals(emptyList(), late)
        }
}
