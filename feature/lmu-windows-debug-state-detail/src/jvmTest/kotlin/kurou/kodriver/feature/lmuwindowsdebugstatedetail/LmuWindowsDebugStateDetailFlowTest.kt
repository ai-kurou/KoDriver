package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsClassCarGapData
import kurou.kodriver.domain.model.LMU_WINDOWS_READOUT_ITEM_KEYS
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.registerLmuWindowsReadoutItemKeys
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsDebugStateDetailFlowTest {
    @BeforeTest
    fun `ReadoutItemKeyを登録する`() {
        registerLmuWindowsReadoutItemKeys()
    }

    @Test
    fun `既定の入力から既定のUiStateを合成する`() =
        runTest {
            val inputs = Inputs()

            assertEquals(LmuWindowsDebugStateDetailUiState(), inputs.flow().first())
            assertEquals(0, inputs.activeSubscriptions)
        }

    @Test
    @Suppress("LongMethod")
    fun `各入力の更新を反映し他の表示項目を保持する`() =
        runTest {
            val inputs = Inputs()
            val flow = inputs.flow()
            val states = mutableListOf<LmuWindowsDebugStateDetailUiState>()
            backgroundScope.launch { flow.collect { states.add(it) } }
            testScheduler.runCurrent()
            var expected = LmuWindowsDebugStateDetailUiState()
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

            inputs.received.update { LmuWindowsDebugStateCardKey.entries.toSet() }
            testScheduler.runCurrent()
            expected = expected.copy(enabledCardKeys = lmuWindowsSupportedCardKeys)
            assertEquals(expected, states.last())

            val lmu =
                LmuWindowsDebugState(
                    virtualEnergyBaseline = LmuWindowsVirtualEnergyBaseline(0.8, 3),
                    classCarGap = LmuWindowsClassCarGapData(1.5, null),
                )
            inputs.lmu.update { lmu }
            testScheduler.runCurrent()
            expected = expected.copy(lmuWindows = lmu)
            assertEquals(expected, states.last())

            val key = LMU_WINDOWS_READOUT_ITEM_KEYS.filterIsInstance<ReadoutItemKey.TopLevel>().last()
            inputs.selected.update { key }
            testScheduler.runCurrent()
            expected = expected.copy(selectedReadoutItemKey = key)
            assertEquals(expected, states.last())

            inputs.text.update { "テスト読み上げ" }
            testScheduler.runCurrent()
            expected = expected.copy(debugReadoutText = "テスト読み上げ")
            assertEquals(expected, states.last())

            val hotkey = ReadoutHotkeyUiState(virtualKeyCode = 65, checkState = HotkeyCheckState.Detected)
            inputs.hotkey.update { hotkey }
            testScheduler.runCurrent()
            expected = expected.copy(readoutHotkey = hotkey)
            assertEquals(expected, states.last())

            inputs.lmu.update { LmuWindowsDebugState() }
            testScheduler.runCurrent()
            assertEquals(expected.copy(lmuWindows = LmuWindowsDebugState()), states.last())
        }

    @Test
    fun `購読時だけ上流を購読し解除後は最新入力で再購読できる`() =
        runTest {
            val inputs = Inputs()
            val flow = inputs.flow()
            assertEquals(0, inputs.activeSubscriptions)

            val firstCollector = backgroundScope.launch { flow.collect {} }
            testScheduler.runCurrent()
            assertEquals(6, inputs.activeSubscriptions)

            val secondCollector = backgroundScope.launch { flow.collect {} }
            testScheduler.runCurrent()
            assertEquals(12, inputs.activeSubscriptions)

            firstCollector.cancel()
            secondCollector.cancel()
            testScheduler.runCurrent()
            assertEquals(0, inputs.activeSubscriptions)

            inputs.text.update { "再購読" }
            assertEquals(
                LmuWindowsDebugStateDetailUiState(debugReadoutText = "再購読"),
                flow.first(),
            )
            assertEquals(0, inputs.activeSubscriptions)
        }

    private class Inputs {
        val cardOrder = MutableStateFlow(defaultDebugStateCardOrder)
        val received = MutableStateFlow(setOf(LmuWindowsDebugStateCardKey.SIMULATOR))
        val lmu = MutableStateFlow(LmuWindowsDebugState())
        val selected = MutableStateFlow(LmuWindowsDebugStateDetailUiState().selectedReadoutItemKey)
        val text = MutableStateFlow("")
        val hotkey = MutableStateFlow(ReadoutHotkeyUiState())
        var activeSubscriptions = 0
            private set

        fun flow(): Flow<LmuWindowsDebugStateDetailUiState> {
            val composition =
                LmuWindowsDebugStateDetailFlow(
                    cardOrder = cardOrder.tracked(),
                    receivedCardKeys = received.tracked(),
                    lmuWindows = lmu.tracked(),
                    selectedReadoutItemKey = selected.tracked(),
                    debugReadoutText = text.tracked(),
                    hotkey = hotkey.tracked(),
                )
            return composition.uiState(composition.detail(composition.header))
        }

        private fun <T> Flow<T>.tracked(): Flow<T> =
            onStart { activeSubscriptions++ }.onCompletion { activeSubscriptions-- }
    }
}
