package kurou.kodriver.feature.lmuwindowsautoreadout.mybestlapdetail

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsAutoReadoutMyBestLapDetailFlowTest {
    @Test
    fun `既定の入力からUiState初期値と一致する状態を合成する`() =
        runTest {
            val inputs = Inputs()

            assertEquals(LmuWindowsAutoReadoutMyBestLapDetailUiState(), inputs.flow().first())
            assertEquals(0, inputs.activeSubscriptions)
        }

    @Test
    fun `3入力の更新と詳細有効キーの有効無効を反映する`() =
        runTest {
            val inputs = Inputs()
            val states = mutableListOf<LmuWindowsAutoReadoutMyBestLapDetailUiState>()
            backgroundScope.launch { inputs.flow().collect { states.add(it) } }
            testScheduler.runCurrent()
            var expected = LmuWindowsAutoReadoutMyBestLapDetailUiState()
            assertEquals(expected, states.last())

            inputs.readoutText.update { "更新{laptime}" }
            testScheduler.runCurrent()
            expected = expected.copy(readoutText = "更新{laptime}")
            assertEquals(expected, states.last())

            inputs.isTextToSpeechAvailable.update { true }
            testScheduler.runCurrent()
            expected = expected.copy(isTextToSpeechAvailable = true)
            assertEquals(expected, states.last())

            val key = LmuWindowsReadoutItemKey.MyBestLap.DetailEnabled
            val enabledStates: Map<ReadoutItemKey, Boolean> = mapOf(key to false)
            inputs.enabledStates.update { enabledStates }
            testScheduler.runCurrent()
            expected = expected.copy(enabled = false)
            assertEquals(expected, states.last())

            inputs.enabledStates.update { mapOf(LmuWindowsReadoutItemKey.MyBestLap.Root to false) }
            testScheduler.runCurrent()
            assertEquals(expected.copy(enabled = true), states.last())

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
            assertEquals(3, inputs.activeSubscriptions)

            collector.cancel()
            testScheduler.runCurrent()
            assertEquals(0, inputs.activeSubscriptions)

            inputs.readoutText.update { "再購読" }
            assertEquals(
                LmuWindowsAutoReadoutMyBestLapDetailUiState(readoutText = "再購読"),
                flow.first(),
            )
            assertEquals(0, inputs.activeSubscriptions)
        }

    private class Inputs {
        val enabledStates = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
        val readoutText = MutableStateFlow(LMU_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT)
        val isTextToSpeechAvailable = MutableStateFlow(false)
        var activeSubscriptions = 0
            private set

        fun flow(): Flow<LmuWindowsAutoReadoutMyBestLapDetailUiState> =
            LmuWindowsAutoReadoutMyBestLapDetailFlow(
                enabledStates = enabledStates.tracked(),
                readoutText = readoutText.tracked(),
                isTextToSpeechAvailable = isTextToSpeechAvailable.tracked(),
            ).uiState

        private fun <T> Flow<T>.tracked(): Flow<T> =
            onStart { activeSubscriptions++ }.onCompletion { activeSubscriptions-- }
    }
}
