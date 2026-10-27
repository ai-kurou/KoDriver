package kurou.kodriver.feature.gt7ps5debugstatedetail

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.GT7_PS5_READOUT_ITEM_KEYS
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.registerGt7Ps5ReadoutItemKeys
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5DebugStateDetailFlowTest {
    @BeforeTest
    fun `ReadoutItemKeyを登録する`() {
        registerGt7Ps5ReadoutItemKeys()
    }

    @Test
    fun `既定の入力から既定のUiStateを合成する`() =
        runTest {
            val inputs = Inputs()

            assertEquals(Gt7Ps5DebugStateDetailUiState(), inputs.flow().first())
            assertEquals(0, inputs.activeSubscriptions)
        }

    @Test
    @Suppress("LongMethod")
    fun `各入力の更新を反映し他の表示項目を保持する`() =
        runTest {
            val inputs = Inputs()
            val flow = inputs.flow()
            val states = mutableListOf<Gt7Ps5DebugStateDetailUiState>()
            backgroundScope.launch { flow.collect { states.add(it) } }
            testScheduler.runCurrent()
            var expected = Gt7Ps5DebugStateDetailUiState()
            assertEquals(expected, states.last())

            val order = defaultDebugStateCardOrder.reversed()
            inputs.cardOrder.update { order }
            testScheduler.runCurrent()
            expected = expected.copy(cardOrder = order)
            assertEquals(expected, states.last())

            inputs.received.update { emptySet() }
            testScheduler.runCurrent()
            expected = expected.copy(enabledCardKeys = emptySet())
            assertEquals(expected, states.last())

            inputs.received.update { Gt7Ps5DebugStateCardKey.entries.toSet() }
            testScheduler.runCurrent()
            expected = expected.copy(enabledCardKeys = gt7Ps5SupportedCardKeys)
            assertEquals(expected, states.last())

            val gt7 = Gt7Ps5DebugState(telemetry = sampleGt7Ps5Telemetry(lapCount = 3))
            inputs.gt7Ps5.update { gt7 }
            testScheduler.runCurrent()
            expected = expected.copy(gt7Ps5 = gt7)
            assertEquals(expected, states.last())

            val key = GT7_PS5_READOUT_ITEM_KEYS.filterIsInstance<ReadoutItemKey.TopLevel>().last()
            inputs.selected.update { key }
            testScheduler.runCurrent()
            expected = expected.copy(selectedReadoutItemKey = key)
            assertEquals(expected, states.last())

            inputs.text.update { "テスト読み上げ" }
            testScheduler.runCurrent()
            expected = expected.copy(debugReadoutText = "テスト読み上げ")
            assertEquals(expected, states.last())

            inputs.gt7Ps5.update { Gt7Ps5DebugState() }
            testScheduler.runCurrent()
            assertEquals(expected.copy(gt7Ps5 = Gt7Ps5DebugState()), states.last())
        }

    @Test
    fun `購読時だけ上流を購読し解除後は最新入力で再購読できる`() =
        runTest {
            val inputs = Inputs()
            val flow = inputs.flow()
            assertEquals(0, inputs.activeSubscriptions)

            val firstCollector = backgroundScope.launch { flow.collect {} }
            testScheduler.runCurrent()
            assertEquals(5, inputs.activeSubscriptions)

            val secondCollector = backgroundScope.launch { flow.collect {} }
            testScheduler.runCurrent()
            assertEquals(10, inputs.activeSubscriptions)

            firstCollector.cancel()
            secondCollector.cancel()
            testScheduler.runCurrent()
            assertEquals(0, inputs.activeSubscriptions)

            inputs.text.update { "再購読" }
            assertEquals(
                Gt7Ps5DebugStateDetailUiState(debugReadoutText = "再購読"),
                flow.first(),
            )
            assertEquals(0, inputs.activeSubscriptions)
        }

    private class Inputs {
        val cardOrder = MutableStateFlow(defaultDebugStateCardOrder)
        val received = MutableStateFlow(setOf(Gt7Ps5DebugStateCardKey.SIMULATOR))
        val gt7Ps5 = MutableStateFlow(Gt7Ps5DebugState())
        val selected = MutableStateFlow(Gt7Ps5DebugStateDetailUiState().selectedReadoutItemKey)
        val text = MutableStateFlow("")
        var activeSubscriptions = 0
            private set

        fun flow(): Flow<Gt7Ps5DebugStateDetailUiState> {
            val composition =
                Gt7Ps5DebugStateDetailFlow(
                    cardOrder = cardOrder.tracked(),
                    receivedCardKeys = received.tracked(),
                    gt7Ps5 = gt7Ps5.tracked(),
                    selectedReadoutItemKey = selected.tracked(),
                    debugReadoutText = text.tracked(),
                )
            return composition.detail(composition.header)
        }

        private fun <T> Flow<T>.tracked(): Flow<T> =
            onStart { activeSubscriptions++ }.onCompletion { activeSubscriptions-- }
    }
}
