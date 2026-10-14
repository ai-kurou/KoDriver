package kurou.kodriver.data.manualreadout

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ManualReadoutRequest
import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertEquals

class InMemoryManualReadoutRequestRepositoryTest {
    @Test
    fun `連続した依頼を全購読者に順番通り配信し過去の依頼は再配信しない`() =
        runTest {
            val repository = InMemoryManualReadoutRequestRepository()
            repository.request(ManualReadoutRequest(Simulator.LmuWindows.id, "購読前"))
            val first = mutableListOf<ManualReadoutRequest>()
            val second = mutableListOf<ManualReadoutRequest>()
            backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
                repository.observeRequests().collect { first.add(it) }
            }
            backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
                repository.observeRequests().collect { second.add(it) }
            }
            // バッファ容量を超えても emit の待機により順序を保って配信する。
            val requests = (1..100).map { ManualReadoutRequest(Simulator.LmuWindows.id, "set-$it") }
            requests.forEach { repository.request(it) }
            testScheduler.runCurrent()
            assertEquals(requests, first)
            assertEquals(requests, second)
            val late = mutableListOf<ManualReadoutRequest>()
            backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
                repository.observeRequests().collect { late.add(it) }
            }
            testScheduler.runCurrent()
            assertEquals(emptyList(), late)
        }
}
