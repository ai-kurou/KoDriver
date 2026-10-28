package kurou.kodriver.feature.gt7ps5autoreadout.remainingfueldetail

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_REMAINING_FUEL_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5AutoReadoutRemainingFuelDetailFlowTest {
    @Test
    fun `既定の入力からUiState初期値と一致する状態を合成する`() =
        runTest {
            val inputs = Inputs()

            assertEquals(Gt7Ps5AutoReadoutRemainingFuelDetailUiState(), inputs.flow().first())
            assertEquals(0, inputs.activeSubscriptions)
        }

    @Test
    fun `4入力の更新と詳細有効キーの有効無効を反映する`() =
        runTest {
            val inputs = Inputs()
            val states = mutableListOf<Gt7Ps5AutoReadoutRemainingFuelDetailUiState>()
            backgroundScope.launch { inputs.flow().collect { states.add(it) } }
            testScheduler.runCurrent()
            var expected = Gt7Ps5AutoReadoutRemainingFuelDetailUiState()
            assertEquals(expected, states.last())

            inputs.thresholdPercentage.update { 45 }
            testScheduler.runCurrent()
            expected = expected.copy(thresholdPercentage = 45)
            assertEquals(expected, states.last())

            inputs.readoutText.update { "更新{percent}" }
            testScheduler.runCurrent()
            expected = expected.copy(readoutText = "更新{percent}")
            assertEquals(expected, states.last())

            inputs.isTextToSpeechAvailable.update { true }
            testScheduler.runCurrent()
            expected = expected.copy(isTextToSpeechAvailable = true)
            assertEquals(expected, states.last())

            val key = Gt7Ps5ReadoutItemKey.RemainingFuel.DetailEnabled
            val enabledStates: Map<ReadoutItemKey, Boolean> = mapOf(key to false)
            inputs.enabledStates.update { enabledStates }
            testScheduler.runCurrent()
            expected = expected.copy(enabled = false)
            assertEquals(expected, states.last())

            inputs.enabledStates.update { mapOf(key to true) }
            testScheduler.runCurrent()
            assertEquals(expected.copy(enabled = true), states.last())
        }

    @Test
    fun `詳細有効キーがなければ他のキーが無効でも有効として合成する`() =
        runTest {
            val inputs = Inputs()
            inputs.enabledStates.update { mapOf(Gt7Ps5ReadoutItemKey.RemainingFuel.Root to false) }
            inputs.thresholdPercentage.update { 0 }
            inputs.readoutText.update { "" }
            inputs.isTextToSpeechAvailable.update { true }

            assertEquals(
                Gt7Ps5AutoReadoutRemainingFuelDetailUiState(
                    thresholdPercentage = 0,
                    readoutText = "",
                    isTextToSpeechAvailable = true,
                    enabled = true,
                ),
                inputs.flow().first(),
            )
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
                Gt7Ps5AutoReadoutRemainingFuelDetailUiState(readoutText = "再購読"),
                flow.first(),
            )
            assertEquals(0, inputs.activeSubscriptions)
        }

    private class Inputs {
        val thresholdPercentage = MutableStateFlow(GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT)
        val enabledStates = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
        val readoutText = MutableStateFlow(GT7_PS5_REMAINING_FUEL_READOUT_TEXT_DEFAULT)
        val isTextToSpeechAvailable = MutableStateFlow(false)
        var activeSubscriptions = 0
            private set

        fun flow(): Flow<Gt7Ps5AutoReadoutRemainingFuelDetailUiState> =
            Gt7Ps5AutoReadoutRemainingFuelDetailFlow(
                thresholdPercentage = thresholdPercentage.tracked(),
                enabledStates = enabledStates.tracked(),
                readoutText = readoutText.tracked(),
                isTextToSpeechAvailable = isTextToSpeechAvailable.tracked(),
            ).uiState

        private fun <T> Flow<T>.tracked(): Flow<T> =
            onStart { activeSubscriptions++ }.onCompletion { activeSubscriptions-- }
    }
}
