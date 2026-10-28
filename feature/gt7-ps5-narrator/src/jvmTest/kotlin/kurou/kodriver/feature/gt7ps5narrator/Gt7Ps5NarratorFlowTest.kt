package kurou.kodriver.feature.gt7ps5narrator

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.gt7ps5.model.Gt7Ps5FuelUnit
import kurou.kodriver.domain.gt7ps5.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.ManualReadoutRequest
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5NarratorFlowTest {
    @Test
    fun `有効状態は両入力を待ち詳細設定を優先して更新し選択変更時に旧設定を解除する`() =
        runTest {
            val selected = MutableStateFlow<Simulator>(Simulator.Gt7Ps5)
            val gt7States = MutableSharedFlow<Map<ReadoutItemKey, Boolean>>()
            val lmuStates = MutableSharedFlow<Map<ReadoutItemKey, Boolean>>()
            val tyreStates = MutableSharedFlow<Map<ReadoutItemKey, Boolean>>()
            val root = Gt7Ps5ReadoutItemKey.TyreTemperature.Root
            val detail = Gt7Ps5ReadoutItemKey.TyreTemperature.OverheatWarning
            val states = mutableListOf<Map<ReadoutItemKey, Boolean>>()
            val observedIds = mutableListOf<String>()
            val flow =
                Gt7Ps5NarratorFlow(selected).enabledStates(
                    observeAutoReadoutEnabledStates = { id ->
                        observedIds.add(id)
                        if (id == Simulator.Gt7Ps5.id) gt7States else lmuStates
                    },
                    tyreTemperatureStates = tyreStates,
                )
            assertEquals(emptyList(), observedIds)
            val job = backgroundScope.launch { flow.collect { states.add(it) } }
            runCurrent()
            gt7States.emit(mapOf<ReadoutItemKey, Boolean>(root to true, detail to true))
            runCurrent()
            assertEquals(emptyList(), states)
            tyreStates.emit(mapOf(detail to false))
            runCurrent()
            assertEquals(mapOf<ReadoutItemKey, Boolean>(root to true, detail to false), states.last())
            tyreStates.emit(mapOf<ReadoutItemKey, Boolean>(detail to true))
            runCurrent()
            assertEquals(mapOf<ReadoutItemKey, Boolean>(root to true, detail to true), states.last())
            gt7States.emit(mapOf(root to false))
            runCurrent()
            assertEquals(mapOf<ReadoutItemKey, Boolean>(root to false, detail to true), states.last())

            selected.update { Simulator.LmuWindows }
            runCurrent()
            assertEquals(0, gt7States.subscriptionCount.value)
            assertEquals(mapOf<ReadoutItemKey, Boolean>(root to false, detail to true), states.last())
            lmuStates.emit(emptyMap())
            runCurrent()
            assertEquals(mapOf<ReadoutItemKey, Boolean>(detail to true), states.last())
            assertEquals(listOf(Simulator.Gt7Ps5.id, Simulator.LmuWindows.id), observedIds)
            job.cancel()
            runCurrent()
            assertEquals(0, lmuStates.subscriptionCount.value)
            assertEquals(0, tyreStates.subscriptionCount.value)
        }

    @Test
    fun `読み上げ順序は選択先の更新を反映し旧購読を解除して再選択で購読し直す`() =
        runTest {
            val selected = MutableStateFlow<Simulator>(Simulator.Gt7Ps5)
            val gt7Order = MutableStateFlow<List<ReadoutItemKey>>(emptyList())
            val aceOrder = MutableStateFlow<List<ReadoutItemKey>>(emptyList())
            val key = Gt7Ps5ReadoutItemKey.MyBestLap.Root
            val observed = mutableListOf<Simulator>()
            val orders = mutableListOf<List<ReadoutItemKey>>()
            val flow =
                Gt7Ps5NarratorFlow(selected).autoReadoutOrder { simulator ->
                    observed.add(simulator)
                    if (simulator == Simulator.Gt7Ps5) gt7Order else aceOrder
                }
            assertEquals(emptyList(), observed)
            val job = backgroundScope.launch { flow.collect { orders.add(it) } }
            runCurrent()
            assertEquals(emptyList(), orders.last())
            gt7Order.update { listOf(key) }
            runCurrent()
            assertEquals(listOf(key), orders.last())
            selected.update { Simulator.AceWindows }
            runCurrent()
            assertEquals(emptyList(), orders.last())
            assertEquals(0, gt7Order.subscriptionCount.value)
            gt7Order.update { emptyList() }
            aceOrder.update { listOf(key) }
            runCurrent()
            assertEquals(listOf(key), orders.last())
            selected.update { Simulator.Gt7Ps5 }
            runCurrent()
            assertEquals(emptyList(), orders.last())
            assertEquals(0, aceOrder.subscriptionCount.value)
            assertEquals(listOf(Simulator.Gt7Ps5, Simulator.AceWindows, Simulator.Gt7Ps5), observed)
            job.cancel()
            runCurrent()
            assertEquals(0, gt7Order.subscriptionCount.value)
        }

    @Test
    fun `テレメトリはGT7選択中だけ購読し切り替えと収集終了でキャンセルする`() =
        runTest {
            val selected = MutableStateFlow<Simulator>(Simulator.LmuWindows)
            val telemetry = MutableSharedFlow<Gt7Ps5TelemetryData>()
            var starts = 0
            var completions = 0
            val flow =
                Gt7Ps5NarratorFlow(selected).telemetry {
                    telemetry.onStart { starts++ }.onCompletion { completions++ }
                }
            val received = mutableListOf<Gt7Ps5TelemetryData>()
            assertEquals(0, starts)
            val job = backgroundScope.launch { flow.collect { received.add(it) } }
            runCurrent()
            assertEquals(0, starts)
            selected.update { Simulator.Gt7Ps5 }
            runCurrent()
            val frame = Gt7Ps5TelemetryData(1, 5, 90_000, Gt7Ps5FuelUnit(50f), Gt7Ps5FuelUnit(100f))
            telemetry.emit(frame)
            telemetry.emit(frame)
            runCurrent()
            assertEquals(listOf(frame, frame), received)
            selected.update { Simulator.AceWindows }
            runCurrent()
            assertEquals(1, completions)
            assertEquals(0, telemetry.subscriptionCount.value)
            telemetry.emit(frame)
            selected.update { Simulator.Gt7Ps5 }
            runCurrent()
            assertEquals(2, starts)
            assertEquals(listOf(frame, frame), received)
            telemetry.emit(frame)
            runCurrent()
            assertEquals(listOf(frame, frame, frame), received)
            job.cancel()
            runCurrent()
            assertEquals(2, completions)
        }

    @Test
    fun `手動要求は選択や宛先で除外せず両経路の重複も保持し終了時に両購読を解除する`() =
        runTest {
            val requests = MutableSharedFlow<ManualReadoutRequest>()
            val hotkeys = MutableSharedFlow<ManualReadoutRequest>()
            val flow =
                Gt7Ps5NarratorFlow(MutableStateFlow(Simulator.LmuWindows))
                    .manualReadoutRequests(requests, hotkeys)
            val received = mutableListOf<ManualReadoutRequest>()
            assertEquals(0, requests.subscriptionCount.value)
            assertEquals(0, hotkeys.subscriptionCount.value)
            val job = backgroundScope.launch { flow.collect { received.add(it) } }
            runCurrent()
            val gt7Request = ManualReadoutRequest(Simulator.Gt7Ps5.id, "set-1")
            val lmuRequest = ManualReadoutRequest(Simulator.LmuWindows.id, "missing")
            requests.emit(gt7Request)
            runCurrent()
            hotkeys.emit(gt7Request)
            runCurrent()
            requests.emit(lmuRequest)
            runCurrent()
            assertEquals(listOf(gt7Request, gt7Request, lmuRequest), received)
            job.cancel()
            runCurrent()
            assertEquals(0, requests.subscriptionCount.value)
            assertEquals(0, hotkeys.subscriptionCount.value)
        }
}
