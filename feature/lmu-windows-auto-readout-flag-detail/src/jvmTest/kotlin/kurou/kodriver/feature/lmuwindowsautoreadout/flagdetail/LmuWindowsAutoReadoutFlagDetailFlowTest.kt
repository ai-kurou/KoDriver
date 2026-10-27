package kurou.kodriver.feature.lmuwindowsautoreadout.flagdetail

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsAutoReadoutFlagDetailFlowTest {
    @Test
    fun `既定の入力から既定のUiStateを合成する`() =
        runTest {
            val inputs = Inputs()

            assertEquals(LmuWindowsAutoReadoutFlagDetailUiState(), inputs.flow().first())
            assertEquals(0, inputs.activeSubscriptions)
        }

    @Test
    fun `各入力の更新を反映する`() =
        runTest {
            val inputs = Inputs()
            val flow = inputs.flow()
            val states = mutableListOf<LmuWindowsAutoReadoutFlagDetailUiState>()
            backgroundScope.launch { flow.collect { states.add(it) } }
            testScheduler.runCurrent()
            var expected = LmuWindowsAutoReadoutFlagDetailUiState()
            assertEquals(expected, states.last())

            val enabledStates: Map<ReadoutItemKey, Boolean> = mapOf(LmuWindowsReadoutItemKey.Flag.BlueFlag to false)
            inputs.enabledStates.update { enabledStates }
            testScheduler.runCurrent()
            expected = expected.copy(enabledStates = enabledStates)
            assertEquals(expected, states.last())

            val flagTexts =
                mapOf(
                    FlagReadoutItem.RedFlag to "レッドフラッグ、停止してください",
                    FlagReadoutItem.FullCourseYellow to "フルコースイエロー、減速してください",
                )
            inputs.flagTexts.update { flagTexts }
            testScheduler.runCurrent()
            expected = expected.copy(flagTexts = flagTexts)
            assertEquals(expected, states.last())

            inputs.isTextToSpeechAvailable.update { true }
            testScheduler.runCurrent()
            expected = expected.copy(isTextToSpeechAvailable = true)
            assertEquals(expected, states.last())

            inputs.flagTexts.update { emptyMap() }
            testScheduler.runCurrent()
            assertEquals(expected.copy(flagTexts = emptyMap()), states.last())
        }

    @Test
    fun `購読時だけ上流を購読し解除後は最新入力で再購読できる`() =
        runTest {
            val inputs = Inputs()
            val flow = inputs.flow()
            assertEquals(0, inputs.activeSubscriptions)

            val firstCollector = backgroundScope.launch { flow.collect {} }
            testScheduler.runCurrent()
            assertEquals(3, inputs.activeSubscriptions)

            val secondCollector = backgroundScope.launch { flow.collect {} }
            testScheduler.runCurrent()
            assertEquals(6, inputs.activeSubscriptions)

            firstCollector.cancel()
            secondCollector.cancel()
            testScheduler.runCurrent()
            assertEquals(0, inputs.activeSubscriptions)

            val flagTexts = mapOf(FlagReadoutItem.SectorYellowFlag to "再購読")
            inputs.flagTexts.update { flagTexts }
            assertEquals(LmuWindowsAutoReadoutFlagDetailUiState(flagTexts = flagTexts), flow.first())
            assertEquals(0, inputs.activeSubscriptions)
        }

    private class Inputs {
        val enabledStates = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
        val flagTexts = MutableStateFlow<Map<FlagReadoutItem, String>>(emptyMap())
        val isTextToSpeechAvailable = MutableStateFlow(false)
        var activeSubscriptions = 0
            private set

        fun flow(): Flow<LmuWindowsAutoReadoutFlagDetailUiState> =
            LmuWindowsAutoReadoutFlagDetailFlow(
                enabledStates = enabledStates.tracked(),
                flagTexts = flagTexts.tracked(),
                isTextToSpeechAvailable = isTextToSpeechAvailable.tracked(),
            ).uiState

        private fun <T> Flow<T>.tracked(): Flow<T> =
            onStart { activeSubscriptions++ }.onCompletion { activeSubscriptions-- }
    }
}
