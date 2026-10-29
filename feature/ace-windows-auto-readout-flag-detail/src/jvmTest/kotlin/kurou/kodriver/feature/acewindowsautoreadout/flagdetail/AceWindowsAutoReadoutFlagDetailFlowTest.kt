package kurou.kodriver.feature.acewindowsautoreadout.flagdetail

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsAutoReadoutFlagDetailFlowTest {
    @Test
    fun `全フラッグの文言FlowからMapを合成する`() =
        runTest {
            val inputs = Inputs()

            assertEquals(
                AceWindowsAutoReadoutFlagDetailUiState(flagTexts = inputs.currentTexts()),
                inputs.flow().first(),
            )
            assertEquals(0, inputs.activeSubscriptions)
        }

    @Test
    fun `各入力の更新を反映する`() =
        runTest {
            val inputs = Inputs()
            val flow = inputs.flow()
            val states = mutableListOf<AceWindowsAutoReadoutFlagDetailUiState>()
            backgroundScope.launch { flow.collect { states.add(it) } }
            testScheduler.runCurrent()
            var expected = AceWindowsAutoReadoutFlagDetailUiState(flagTexts = inputs.currentTexts())
            assertEquals(expected, states.last())

            val enabledStates: Map<ReadoutItemKey, Boolean> = mapOf(AceWindowsReadoutItemKey.Flag.BlueFlag to false)
            inputs.enabledStates.update { enabledStates }
            testScheduler.runCurrent()
            expected = expected.copy(enabledStates = enabledStates)
            assertEquals(expected, states.last())

            FlagReadoutItem.entries.forEach { item ->
                val text = "更新: ${item.name}"
                inputs.flagTexts.getValue(item).update { text }
                testScheduler.runCurrent()
                expected = expected.copy(flagTexts = expected.flagTexts + (item to text))
                assertEquals(expected, states.last())
            }

            inputs.isTextToSpeechAvailable.update { true }
            testScheduler.runCurrent()
            expected = expected.copy(isTextToSpeechAvailable = true)
            assertEquals(expected, states.last())

            inputs.flagTexts.getValue(FlagReadoutItem.RedFlag).update { "" }
            testScheduler.runCurrent()
            assertEquals(expected.copy(flagTexts = expected.flagTexts + (FlagReadoutItem.RedFlag to "")), states.last())
        }

    @Test
    fun `購読時だけ上流を購読し解除後は最新入力で再購読できる`() =
        runTest {
            val inputs = Inputs()
            val flow = inputs.flow()
            assertEquals(0, inputs.activeSubscriptions)

            val firstCollector = backgroundScope.launch { flow.collect {} }
            testScheduler.runCurrent()
            assertEquals(FlagReadoutItem.entries.size + 2, inputs.activeSubscriptions)

            val secondCollector = backgroundScope.launch { flow.collect {} }
            testScheduler.runCurrent()
            assertEquals((FlagReadoutItem.entries.size + 2) * 2, inputs.activeSubscriptions)

            firstCollector.cancel()
            secondCollector.cancel()
            testScheduler.runCurrent()
            assertEquals(0, inputs.activeSubscriptions)

            FlagReadoutItem.entries.forEach { item ->
                inputs.flagTexts.getValue(item).update { "再購読: ${item.name}" }
            }
            assertEquals(
                AceWindowsAutoReadoutFlagDetailUiState(flagTexts = inputs.currentTexts()),
                flow.first(),
            )
            assertEquals(0, inputs.activeSubscriptions)
        }

    private class Inputs {
        val enabledStates = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
        val flagTexts = FlagReadoutItem.entries.associateWith { MutableStateFlow("初期: ${it.name}") }
        val isTextToSpeechAvailable = MutableStateFlow(false)
        var activeSubscriptions = 0
            private set

        fun currentTexts(): Map<FlagReadoutItem, String> = flagTexts.mapValues { (_, texts) -> texts.value }

        fun flow(): Flow<AceWindowsAutoReadoutFlagDetailUiState> =
            AceWindowsAutoReadoutFlagDetailFlow(
                enabledStates = enabledStates.tracked(),
                flagTextFlows = flagTexts.mapValues { (_, texts) -> texts.tracked() },
                isTextToSpeechAvailable = isTextToSpeechAvailable.tracked(),
            ).uiState

        private fun <T> Flow<T>.tracked(): Flow<T> =
            onStart { activeSubscriptions++ }.onCompletion { activeSubscriptions-- }
    }
}
