package kurou.kodriver.feature.gt7ps5autoreadout.tyretemperaturedetail

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5AutoReadoutTyreTemperatureDetailFlowTest {
    @Test
    fun `既定の入力からUiState初期値と一致する状態を合成する`() =
        runTest {
            val inputs = Inputs()

            val state = Gt7Ps5AutoReadoutTyreTemperatureDetailUiState()
            assertEquals(GT7_PS5_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT, state.readoutText)
            assertEquals(GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT.value, state.highThresholdCelsius)
            assertEquals(false, state.isTextToSpeechAvailable)
            assertEquals(true, state.overheatWarningEnabled)
            assertEquals(state, inputs.flow().first())
            assertEquals(0, inputs.activeSubscriptions)
        }

    @Test
    fun `4入力の更新と詳細有効キーの有効無効を反映する`() =
        runTest {
            val inputs = Inputs()
            val states = mutableListOf<Gt7Ps5AutoReadoutTyreTemperatureDetailUiState>()
            backgroundScope.launch { inputs.flow().collect { states.add(it) } }
            testScheduler.runCurrent()
            var expected = Gt7Ps5AutoReadoutTyreTemperatureDetailUiState()
            assertEquals(expected, states.last())

            inputs.highThresholdCelsius.update { Celsius(105) }
            testScheduler.runCurrent()
            expected = expected.copy(highThresholdCelsius = 105)
            assertEquals(expected, states.last())

            inputs.readoutText.update { "注意{celsius}度" }
            testScheduler.runCurrent()
            expected = expected.copy(readoutText = "注意{celsius}度")
            assertEquals(expected, states.last())

            inputs.isTextToSpeechAvailable.update { true }
            testScheduler.runCurrent()
            expected = expected.copy(isTextToSpeechAvailable = true)
            assertEquals(expected, states.last())

            val key = Gt7Ps5ReadoutItemKey.TyreTemperature.OverheatWarning
            val enabledStates: Map<ReadoutItemKey, Boolean> = mapOf(key to false)
            inputs.enabledStates.update { enabledStates }
            testScheduler.runCurrent()
            expected = expected.copy(overheatWarningEnabled = false)
            assertEquals(expected, states.last())
            val state = states.last()
            assertEquals("注意{celsius}度", state.readoutText)
            assertEquals(true, state.isTextToSpeechAvailable)
            assertEquals(false, state.overheatWarningEnabled)
            assertEquals(105, state.highThresholdCelsius)

            inputs.enabledStates.update { mapOf(key to true) }
            testScheduler.runCurrent()
            assertEquals(expected.copy(overheatWarningEnabled = true), states.last())
        }

    @Test
    fun `購読開始解除後に最新入力で再購読できる`() =
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

            inputs.readoutText.update { "再購読" }
            assertEquals(
                Gt7Ps5AutoReadoutTyreTemperatureDetailUiState(readoutText = "再購読"),
                flow.first(),
            )
            assertEquals(0, inputs.activeSubscriptions)
        }

    private class Inputs {
        val highThresholdCelsius = MutableStateFlow(GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT)
        val enabledStates =
            MutableStateFlow<Map<ReadoutItemKey, Boolean>>(
                mapOf(
                    Gt7Ps5ReadoutItemKey.TyreTemperature.OverheatWarning to true,
                    Gt7Ps5ReadoutItemKey.TyreTemperature.Root to false,
                ),
            )
        val readoutText = MutableStateFlow(GT7_PS5_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT)
        val isTextToSpeechAvailable = MutableStateFlow(false)
        var activeSubscriptions = 0
            private set

        fun flow(): Flow<Gt7Ps5AutoReadoutTyreTemperatureDetailUiState> =
            Gt7Ps5AutoReadoutTyreTemperatureDetailFlow(
                highThresholdCelsius = highThresholdCelsius.tracked(),
                enabledStates = enabledStates.tracked(),
                readoutText = readoutText.tracked(),
                isTextToSpeechAvailable = isTextToSpeechAvailable.tracked(),
            ).uiState

        private fun <T> Flow<T>.tracked(): Flow<T> =
            onStart { activeSubscriptions++ }.onCompletion { activeSubscriptions-- }
    }
}
