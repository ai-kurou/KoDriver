package kurou.kodriver.feature.othervolumedetail

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OtherVolumeDetailFlowTest {
    private val soundVolume = MutableStateFlow(80)
    private val deviceVolume = MutableStateFlow(60)
    private val isPreviewing = MutableStateFlow(false)
    private val lowVolumeWarningEnabled = MutableStateFlow(true)

    @Test
    fun `音量関連の各状態の更新をUiStateへ反映する`() =
        runTest {
            val flow =
                OtherVolumeDetailFlow(
                    soundVolume = soundVolume,
                    deviceVolume = deviceVolume,
                    isPreviewing = isPreviewing,
                    lowVolumeWarningEnabled = lowVolumeWarningEnabled,
                )
            val states = mutableListOf<OtherVolumeDetailUiState>()
            val subscription = flow.uiState.onEach { states.add(it) }.launchIn(backgroundScope)
            runCurrent()

            soundVolume.update { 40 }
            runCurrent()
            deviceVolume.update { 30 }
            runCurrent()
            isPreviewing.update { true }
            runCurrent()
            lowVolumeWarningEnabled.update { false }
            runCurrent()

            assertEquals(
                listOf(
                    OtherVolumeDetailUiState(volume = 80, deviceVolume = 60),
                    OtherVolumeDetailUiState(volume = 40, deviceVolume = 60),
                    OtherVolumeDetailUiState(volume = 40, deviceVolume = 30),
                    OtherVolumeDetailUiState(volume = 40, deviceVolume = 30, isPreviewing = true),
                    OtherVolumeDetailUiState(
                        volume = 40,
                        deviceVolume = 30,
                        isPreviewing = true,
                        lowVolumeWarningEnabled = false,
                    ),
                ),
                states,
            )
            subscription.cancel()
        }

    @Test
    fun `購読するまで入力を収集せず購読ごとに収集する`() =
        runTest {
            var collections = 0
            val observedVolume =
                flow {
                    collections++
                    emit(72)
                }
            val flow =
                OtherVolumeDetailFlow(
                    soundVolume = observedVolume,
                    deviceVolume = deviceVolume,
                    isPreviewing = isPreviewing,
                    lowVolumeWarningEnabled = lowVolumeWarningEnabled,
                )

            assertEquals(0, collections)
            assertEquals(OtherVolumeDetailUiState(volume = 72, deviceVolume = 60), flow.uiState.first())
            assertEquals(1, collections)
            assertEquals(OtherVolumeDetailUiState(volume = 72, deviceVolume = 60), flow.uiState.first())
            assertEquals(2, collections)
        }
}
