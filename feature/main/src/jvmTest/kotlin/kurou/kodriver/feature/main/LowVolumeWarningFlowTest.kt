package kurou.kodriver.feature.main

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LowVolumeWarning
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class LowVolumeWarningFlowTest {
    @Test
    fun `警告の音量を表示状態へ反映する`() =
        runTest {
            listOf(
                LowVolumeWarning(deviceVolume = 20, soundVolume = null),
                LowVolumeWarning(deviceVolume = null, soundVolume = 30),
                LowVolumeWarning(deviceVolume = 20, soundVolume = 30),
            ).forEach { warning ->
                val flow = LowVolumeWarningFlow(flowOf(warning), flowOf(false)) { error("警告中はリセットしない") }

                assertEquals(
                    LowVolumeWarningUiState(warning.deviceVolume, warning.soundVolume),
                    flow.uiState.first(),
                )
            }
        }

    @Test
    fun `閉じた警告は音量が変化しても非表示を維持する`() =
        runTest {
            val warnings = MutableStateFlow<LowVolumeWarning?>(LowVolumeWarning(20, 30))
            val dismissed = MutableStateFlow(false)
            val flow = LowVolumeWarningFlow(warnings, dismissed) { error("警告中はリセットしない") }
            val states = mutableListOf<LowVolumeWarningUiState>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                flow.uiState.collect { states += it }
            }
            runCurrent()

            dismissed.update { true }
            runCurrent()
            assertEquals(LowVolumeWarningUiState(), states.last())
            warnings.update { LowVolumeWarning(10, 15) }
            runCurrent()
            assertEquals(LowVolumeWarningUiState(), states.last())
        }

    @Test
    fun `警告解消で閉じた状態をリセットし次の警告を表示する`() =
        runTest {
            val warnings = MutableStateFlow<LowVolumeWarning?>(LowVolumeWarning(20, null))
            val dismissed = MutableStateFlow(true)
            val flow = LowVolumeWarningFlow(warnings, dismissed) { dismissed.update { false } }
            val states = mutableListOf<LowVolumeWarningUiState>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                flow.uiState.collect { states += it }
            }
            runCurrent()
            assertEquals(LowVolumeWarningUiState(), states.last())

            warnings.update { null }
            runCurrent()
            assertFalse(dismissed.value)
            assertEquals(LowVolumeWarningUiState(), states.last())
            warnings.update { LowVolumeWarning(25, null) }
            runCurrent()
            assertEquals(LowVolumeWarningUiState(deviceVolume = 25), states.last())
        }

    @Test
    fun `警告なしの初期状態は購読時だけリセットし再購読でも非表示を返す`() =
        runTest {
            val warnings = MutableStateFlow<LowVolumeWarning?>(null)
            var resets = 0
            val flow = LowVolumeWarningFlow(warnings, flowOf(true)) { resets++ }
            assertEquals(0, resets)
            assertEquals(0, warnings.subscriptionCount.value)

            repeat(2) {
                assertEquals(LowVolumeWarningUiState(), flow.uiState.first())
            }
            assertEquals(2, resets)
            assertEquals(0, warnings.subscriptionCount.value)
        }
}
