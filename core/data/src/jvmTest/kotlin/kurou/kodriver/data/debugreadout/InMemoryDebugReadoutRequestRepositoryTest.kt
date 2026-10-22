package kurou.kodriver.data.debugreadout

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.registerReadoutItemKeys
import kurou.kodriver.domain.model.DebugReadoutRequest
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class InMemoryDebugReadoutRequestRepositoryTest {
    @BeforeTest
    fun registerReadoutKeys() {
        registerReadoutItemKeys()
    }

    @Test
    fun `全購読者に要求を配信し過去の要求は再配信しない`() =
        runTest {
            val repository = InMemoryDebugReadoutRequestRepository()
            val request =
                DebugReadoutRequest(
                    Simulator.LmuWindows,
                    ReadoutItemKey.topLevelEntriesOf(Simulator.LmuWindows).first(),
                    "そのまま {laps}",
                )
            repository.requestReadout(request.copy(text = "購読前"))
            val first = mutableListOf<DebugReadoutRequest>()
            val second = mutableListOf<DebugReadoutRequest>()
            backgroundScope.launch(
                start = CoroutineStart.UNDISPATCHED,
            ) { repository.observeRequests().collect { first.add(it) } }
            backgroundScope.launch(
                start = CoroutineStart.UNDISPATCHED,
            ) { repository.observeRequests().collect { second.add(it) } }
            repository.requestReadout(request)
            testScheduler.runCurrent()
            assertEquals(listOf(request), first)
            assertEquals(listOf(request), second)
            val late = mutableListOf<DebugReadoutRequest>()
            backgroundScope.launch(
                start = CoroutineStart.UNDISPATCHED,
            ) { repository.observeRequests().collect { late.add(it) } }
            testScheduler.runCurrent()
            assertEquals(emptyList(), late)
        }
}
