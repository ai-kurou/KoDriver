package kurou.kodriver.data.manualreadout

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ManualReadoutItemRequest
import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertEquals

class InMemoryManualReadoutItemSpokenRepositoryTest {
    @Test
    fun `読み上げた依頼を購読者へ届け購読前の通知は再配信しない`() =
        runTest {
            val repository = InMemoryManualReadoutItemSpokenRepository()
            repository.notifySpoken(ManualReadoutItemRequest(Simulator.LmuWindows.id, "before"))
            val received = mutableListOf<ManualReadoutItemRequest>()
            backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
                repository.observeSpoken().collect { received.add(it) }
            }
            val request = ManualReadoutItemRequest(Simulator.LmuWindows.id, "item", matchedPhrase = "語句")
            repository.notifySpoken(request)
            testScheduler.runCurrent()
            assertEquals(listOf(request), received)
        }

    @Test
    fun `購読側が滞っても通知は待たされず最新を届ける`() =
        runTest {
            val repository = InMemoryManualReadoutItemSpokenRepository()
            val received = mutableListOf<ManualReadoutItemRequest>()
            backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
                repository.observeSpoken().collect { received.add(it) }
            }
            val requests = (1..50).map { ManualReadoutItemRequest(Simulator.LmuWindows.id, "item-$it") }
            requests.forEach { repository.notifySpoken(it) }
            testScheduler.runCurrent()
            assertEquals(requests.last(), received.last())
        }
}
