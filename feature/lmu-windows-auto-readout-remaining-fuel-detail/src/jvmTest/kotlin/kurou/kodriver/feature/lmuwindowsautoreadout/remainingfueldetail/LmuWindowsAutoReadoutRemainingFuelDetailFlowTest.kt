package kurou.kodriver.feature.lmuwindowsautoreadout.remainingfueldetail

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsAutoReadoutRemainingFuelDetailFlowTest {
    @Test
    fun `入力からUiStateを合成し各更新を反映する`() =
        runTest {
            val inputs = Inputs()
            val flow = inputs.flow()
            val states = mutableListOf<LmuWindowsAutoReadoutRemainingFuelDetailUiState>()
            backgroundScope.launch { flow.collect { states.add(it) } }
            testScheduler.runCurrent()
            var expected = LmuWindowsAutoReadoutRemainingFuelDetailUiState()
            assertEquals(expected, states.last())

            inputs.thresholdPercentages.update { 45 }
            testScheduler.runCurrent()
            expected = expected.copy(thresholdPercentage = 45)
            assertEquals(expected, states.last())

            inputs.readoutTexts.update { "燃料残り{percent}%" }
            testScheduler.runCurrent()
            expected = expected.copy(readoutText = "燃料残り{percent}%")
            assertEquals(expected, states.last())

            inputs.isTextToSpeechAvailable.update { true }
            testScheduler.runCurrent()
            expected = expected.copy(isTextToSpeechAvailable = true)
            assertEquals(expected, states.last())

            inputs.enabledStates.update { mapOf(LmuWindowsReadoutItemKey.RemainingFuel.Root to true) }
            testScheduler.runCurrent()
            assertEquals(expected, states.last())

            inputs.enabledStates.update { mapOf(LmuWindowsReadoutItemKey.RemainingFuel.WarningReadout to false) }
            testScheduler.runCurrent()
            expected = expected.copy(enabled = false)
            assertEquals(expected, states.last())
        }

    @Test
    fun `購読時だけ上流を購読し解除後は最新入力で再購読できる`() =
        runTest {
            val inputs = Inputs()
            val flow = inputs.flow()
            assertEquals(0, inputs.activeSubscriptions)

            val collector = backgroundScope.launch { flow.collect {} }
            testScheduler.runCurrent()
            assertEquals(4, inputs.activeSubscriptions)
            collector.cancel()
            testScheduler.runCurrent()
            assertEquals(0, inputs.activeSubscriptions)

            inputs.thresholdPercentages.update { 60 }
            assertEquals(60, flow.first().thresholdPercentage)
            assertEquals(0, inputs.activeSubscriptions)
        }

    private class Inputs {
        val thresholdPercentages = MutableStateFlow(30)
        val enabledStates = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
        val readoutTexts = MutableStateFlow(LMU_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT)
        val isTextToSpeechAvailable = MutableStateFlow(false)
        var activeSubscriptions = 0
            private set

        fun flow(): Flow<LmuWindowsAutoReadoutRemainingFuelDetailUiState> =
            LmuWindowsAutoReadoutRemainingFuelDetailFlow(
                thresholdPercentages = thresholdPercentages.tracked(),
                enabledStates = enabledStates.tracked(),
                readoutTexts = readoutTexts.tracked(),
                isTextToSpeechAvailable = isTextToSpeechAvailable.tracked(),
            ).uiState

        private fun <T> Flow<T>.tracked(): Flow<T> =
            onStart { activeSubscriptions++ }.onCompletion { activeSubscriptions-- }
    }
}
