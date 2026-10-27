package kurou.kodriver.feature.acewindowsautoreadout.remainingfuellapsdetail

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsAutoReadoutRemainingFuelLapsDetailFlowTest {
    @Test
    fun `既定の入力からUiState初期値と一致する状態を合成する`() =
        runTest {
            val inputs = Inputs()

            assertEquals(AceWindowsAutoReadoutRemainingFuelLapsDetailUiState(), inputs.flow().first())
            assertEquals(0, inputs.activeSubscriptions)
        }

    @Test
    fun `5入力の個別更新と詳細有効キーの有効無効を反映する`() =
        runTest {
            val inputs = Inputs()
            val states = mutableListOf<AceWindowsAutoReadoutRemainingFuelLapsDetailUiState>()
            backgroundScope.launch { inputs.flow().collect { states.add(it) } }
            testScheduler.runCurrent()
            var expected = AceWindowsAutoReadoutRemainingFuelLapsDetailUiState()
            assertEquals(expected, states.last())

            inputs.remainingFuelLaps.update { it + 1 }
            testScheduler.runCurrent()
            expected = expected.copy(remainingFuelLaps = ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT + 1)
            assertEquals(expected, states.last())

            val key = AceWindowsReadoutItemKey.RemainingFuelLaps.DetailEnabled
            inputs.enabledStates.update { mapOf(key to false) }
            testScheduler.runCurrent()
            expected = expected.copy(enabled = false)
            assertEquals(expected, states.last())

            inputs.readoutText.update { "更新{laps}" }
            testScheduler.runCurrent()
            expected = expected.copy(readoutText = "更新{laps}")
            assertEquals(expected, states.last())

            inputs.emptyReadoutText.update { "更新0周" }
            testScheduler.runCurrent()
            expected = expected.copy(emptyReadoutText = "更新0周")
            assertEquals(expected, states.last())

            inputs.isTextToSpeechAvailable.update { true }
            testScheduler.runCurrent()
            expected = expected.copy(isTextToSpeechAvailable = true)
            assertEquals(expected, states.last())

            inputs.enabledStates.update { mapOf(key to true) }
            testScheduler.runCurrent()
            assertEquals(expected.copy(enabled = true), states.last())
        }

    @Test
    fun `購読開始解除後に最新入力で再購読できる`() =
        runTest {
            val inputs = Inputs()
            val flow = inputs.flow()
            assertEquals(0, inputs.activeSubscriptions)

            val collector = backgroundScope.launch { flow.collect {} }
            testScheduler.runCurrent()
            assertEquals(5, inputs.activeSubscriptions)

            collector.cancel()
            testScheduler.runCurrent()
            assertEquals(0, inputs.activeSubscriptions)

            inputs.readoutText.update { "再購読" }
            assertEquals(
                AceWindowsAutoReadoutRemainingFuelLapsDetailUiState(readoutText = "再購読"),
                flow.first(),
            )
            assertEquals(0, inputs.activeSubscriptions)
        }

    private class Inputs {
        val remainingFuelLaps = MutableStateFlow(ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT)
        val enabledStates = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
        val readoutText = MutableStateFlow(ACE_WINDOWS_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT)
        val emptyReadoutText = MutableStateFlow(ACE_WINDOWS_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT)
        val isTextToSpeechAvailable = MutableStateFlow(false)
        var activeSubscriptions = 0
            private set

        fun flow(): Flow<AceWindowsAutoReadoutRemainingFuelLapsDetailUiState> =
            AceWindowsAutoReadoutRemainingFuelLapsDetailFlow(
                remainingFuelLaps = remainingFuelLaps.tracked(),
                enabledStates = enabledStates.tracked(),
                readoutText = readoutText.tracked(),
                emptyReadoutText = emptyReadoutText.tracked(),
                isTextToSpeechAvailable = isTextToSpeechAvailable.tracked(),
            ).uiState

        private fun <T> Flow<T>.tracked(): Flow<T> =
            onStart { activeSubscriptions++ }.onCompletion { activeSubscriptions-- }
    }
}
