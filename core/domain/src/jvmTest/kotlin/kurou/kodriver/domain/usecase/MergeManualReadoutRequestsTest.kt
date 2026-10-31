package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ManualReadoutRequest
import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertEquals

class MergeManualReadoutRequestsTest {
    @Test
    fun `両経路の重複と別シミュレーター宛てもそのまま合流し終了時に両購読を解除する`() =
        runTest {
            val requests = MutableSharedFlow<ManualReadoutRequest>()
            val hotkeys = MutableSharedFlow<ManualReadoutRequest>()
            val values = mutableListOf<ManualReadoutRequest>()
            val job =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    mergeManualReadoutRequests(requests, hotkeys).collect { values += it }
                }
            runCurrent()
            val ace = ManualReadoutRequest(Simulator.AceWindows.id, "ace")
            val other = ManualReadoutRequest(Simulator.LmuWindows.id, "lmu")
            requests.emit(ace)
            runCurrent()
            hotkeys.emit(ace)
            runCurrent()
            requests.emit(other)
            runCurrent()
            assertEquals(listOf(ace, ace, other), values)
            job.cancel()
            runCurrent()
            assertEquals(0, requests.subscriptionCount.value)
            assertEquals(0, hotkeys.subscriptionCount.value)
        }
}
