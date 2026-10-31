package kurou.kodriver.feature.acewindowsnarrator

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsNarratorFlowTest {
    @Test
    fun `テレメトリは収集時だけACEを購読し切替と収集終了でキャンセルする`() =
        runTest {
            val selected = MutableStateFlow<Simulator>(Simulator.LmuWindows)
            val telemetry = MutableSharedFlow<Int>()
            var observations = 0
            val flow =
                AceWindowsNarratorFlow(selected).telemetry {
                    observations++
                    telemetry
                }
            val values = mutableListOf<Int>()
            assertEquals(0, observations)
            val job =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    flow.collect { values += it }
                }
            runCurrent()
            assertEquals(0, observations)
            selected.update { Simulator.Gt7Ps5 }
            runCurrent()
            assertEquals(0, observations)
            selected.update { Simulator.AceWindows }
            runCurrent()
            assertEquals(1, telemetry.subscriptionCount.value)
            telemetry.emit(1)
            telemetry.emit(1)
            runCurrent()
            assertEquals(listOf(1, 1), values)
            selected.update { Simulator.LmuWindows }
            runCurrent()
            assertEquals(0, telemetry.subscriptionCount.value)
            telemetry.emit(2)
            runCurrent()
            assertEquals(listOf(1, 1), values)
            selected.update { Simulator.AceWindows }
            runCurrent()
            assertEquals(2, observations)
            assertEquals(listOf(1, 1), values)
            telemetry.emit(3)
            runCurrent()
            assertEquals(listOf(1, 1, 3), values)
            job.cancel()
            runCurrent()
            assertEquals(0, telemetry.subscriptionCount.value)
        }

    @Test
    fun `一覧設定と順序は選択先を渡し古い購読をキャンセルする`() =
        runTest {
            val selected = MutableStateFlow<Simulator>(Simulator.AceWindows)
            val flow = AceWindowsNarratorFlow(selected)
            val key = AceWindowsReadoutItemKey.RemainingFuel.Root
            val aceStates = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(mapOf(key to true))
            val otherStates = MutableSharedFlow<Map<ReadoutItemKey, Boolean>>()
            val aceOrder = MutableStateFlow<List<ReadoutItemKey>>(listOf(key))
            val otherOrder = MutableSharedFlow<List<ReadoutItemKey>>()
            val ids = mutableListOf<String>()
            val simulators = mutableListOf<Simulator>()
            val states = mutableListOf<Map<ReadoutItemKey, Boolean>>()
            val orders = mutableListOf<List<ReadoutItemKey>>()
            val statesJob =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    flow
                        .listEnabledStates { id ->
                            ids += id
                            if (id == Simulator.AceWindows.id) aceStates else otherStates
                        }.collect { states += it }
                }
            val orderJob =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    flow
                        .autoReadoutOrder { simulator ->
                            simulators += simulator
                            if (simulator == Simulator.AceWindows) aceOrder else otherOrder
                        }.collect { orders += it }
                }
            runCurrent()
            selected.update { Simulator.Gt7Ps5 }
            runCurrent()
            assertEquals(0, aceStates.subscriptionCount.value)
            assertEquals(0, aceOrder.subscriptionCount.value)
            // 新しい設定が未受信の間は空値を追加しない。
            assertEquals(listOf<Map<ReadoutItemKey, Boolean>>(mapOf(key to true)), states)
            assertEquals(listOf<List<ReadoutItemKey>>(listOf(key)), orders)
            otherStates.emit(mapOf(key to false))
            otherOrder.emit(emptyList())
            runCurrent()
            assertEquals(mapOf<ReadoutItemKey, Boolean>(key to false), states.last())
            assertEquals(emptyList<ReadoutItemKey>(), orders.last())
            selected.update { Simulator.AceWindows }
            runCurrent()
            assertEquals(0, otherStates.subscriptionCount.value)
            assertEquals(0, otherOrder.subscriptionCount.value)
            assertEquals(listOf(Simulator.AceWindows.id, Simulator.Gt7Ps5.id, Simulator.AceWindows.id), ids)
            assertEquals(listOf(Simulator.AceWindows, Simulator.Gt7Ps5, Simulator.AceWindows), simulators)
            assertEquals(mapOf<ReadoutItemKey, Boolean>(key to true), states.last())
            assertEquals(listOf<ReadoutItemKey>(key), orders.last())
            statesJob.cancel()
            orderJob.cancel()
            runCurrent()
            assertEquals(0, aceStates.subscriptionCount.value)
            assertEquals(0, aceOrder.subscriptionCount.value)
        }

    @Test
    fun `有効設定は全ソースを待って後勝ちで合成し更新を反映する`() =
        runTest {
            val key = AceWindowsReadoutItemKey.RemainingFuel.Root
            val flagKey = AceWindowsReadoutItemKey.Flag.Root
            val list = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(mapOf(key to false, flagKey to true))
            val flags = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(mapOf(key to true))
            val tyres = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(mapOf(key to false))
            val approach = MutableSharedFlow<Map<ReadoutItemKey, Boolean>>(replay = 1)
            val values = mutableListOf<Map<ReadoutItemKey, Boolean>>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                AceWindowsNarratorFlow(emptyFlow())
                    .mergedEnabledStates(list, flags, tyres, approach)
                    .collect { values += it }
            }
            runCurrent()
            assertEquals(emptyList(), values)
            approach.emit(mapOf(key to true))
            runCurrent()
            assertEquals(mapOf<ReadoutItemKey, Boolean>(key to true, flagKey to true), values.last())
            approach.emit(emptyMap())
            runCurrent()
            assertEquals(mapOf<ReadoutItemKey, Boolean>(key to false, flagKey to true), values.last())
            tyres.update { emptyMap() }
            runCurrent()
            assertEquals(mapOf<ReadoutItemKey, Boolean>(key to true, flagKey to true), values.last())
            flags.update { emptyMap() }
            list.update { mapOf(key to true) }
            runCurrent()
            assertEquals(mapOf<ReadoutItemKey, Boolean>(key to true), values.last())
        }
}
