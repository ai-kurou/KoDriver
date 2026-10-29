package kurou.kodriver.feature.othervolumedetail

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.usecase.GetDeviceVolumeUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OtherVolumeDetailFlowTest {
    private val soundVolume = MutableStateFlow(80)
    private val deviceVolumeRefreshTrigger = MutableStateFlow(0)
    private val getDeviceVolume: GetDeviceVolumeUseCase = mockk()
    private val isPreviewing = MutableStateFlow(false)
    private val lowVolumeWarningEnabled = MutableStateFlow(true)

    @Test
    fun `音量関連の各状態の更新をUiStateへ反映する`() =
        runTest {
            coEvery { getDeviceVolume() } returnsMany listOf(60, 30)
            val flow =
                OtherVolumeDetailFlow(
                    soundVolume = soundVolume,
                    deviceVolumeRefreshTrigger = deviceVolumeRefreshTrigger,
                    getDeviceVolume = { getDeviceVolume() },
                    isPreviewing = isPreviewing,
                    lowVolumeWarningEnabled = lowVolumeWarningEnabled,
                )
            val states = mutableListOf<OtherVolumeDetailUiState>()
            val subscription = flow.uiState.onEach { states.add(it) }.launchIn(backgroundScope)
            runCurrent()

            soundVolume.update { 40 }
            runCurrent()
            deviceVolumeRefreshTrigger.update { it + 1 }
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
            coVerify(exactly = 2) { getDeviceVolume() }
            confirmVerified(getDeviceVolume)
        }

    @Test
    fun `購読するまで入力を収集せず購読ごとに収集する`() =
        runTest {
            coEvery { getDeviceVolume() } returns 60
            var collections = 0
            val observedVolume =
                flow {
                    collections++
                    emit(72)
                }
            val flow =
                OtherVolumeDetailFlow(
                    soundVolume = observedVolume,
                    deviceVolumeRefreshTrigger = deviceVolumeRefreshTrigger,
                    getDeviceVolume = { getDeviceVolume() },
                    isPreviewing = isPreviewing,
                    lowVolumeWarningEnabled = lowVolumeWarningEnabled,
                )

            advanceTimeBy(1_000)
            assertEquals(0, collections)
            coVerify(exactly = 0) { getDeviceVolume() }
            assertEquals(OtherVolumeDetailUiState(volume = 72, deviceVolume = 60), flow.uiState.first())
            assertEquals(1, collections)
            assertEquals(OtherVolumeDetailUiState(volume = 72, deviceVolume = 60), flow.uiState.first())
            assertEquals(2, collections)
            advanceTimeBy(1_000)
            coVerify(exactly = 2) { getDeviceVolume() }
            confirmVerified(getDeviceVolume)
        }

    @Test
    fun `購読中は500msごとに端末音量を再取得し解除すると停止する`() =
        runTest {
            coEvery { getDeviceVolume() } returnsMany listOf(60, 30, 20)
            val states = mutableListOf<OtherVolumeDetailUiState>()
            val subscription = createFlow().uiState.onEach { states.add(it) }.launchIn(backgroundScope)
            runCurrent()
            assertEquals(listOf(60), states.map { it.deviceVolume })

            advanceTimeBy(499)
            runCurrent()
            assertEquals(listOf(60), states.map { it.deviceVolume })
            advanceTimeBy(1)
            runCurrent()
            advanceTimeBy(500)
            runCurrent()
            assertEquals(listOf(60, 30, 20), states.map { it.deviceVolume })

            subscription.cancel()
            runCurrent()
            advanceTimeBy(1_000)
            coVerify(exactly = 3) { getDeviceVolume() }
            confirmVerified(getDeviceVolume)
        }

    @Test
    fun `更新通知と定期取得は進行中の古い読み取りをキャンセルする`() =
        runTest {
            var reads = 0
            var cancellations = 0
            coEvery { getDeviceVolume() } coAnswers {
                if (++reads <= 2) {
                    try {
                        awaitCancellation()
                    } finally {
                        cancellations++
                    }
                } else {
                    30
                }
            }
            val states = mutableListOf<OtherVolumeDetailUiState>()
            val subscription = createFlow().uiState.onEach { states.add(it) }.launchIn(backgroundScope)
            runCurrent()
            deviceVolumeRefreshTrigger.update { it + 1 }
            runCurrent()
            assertEquals(1, cancellations)
            advanceTimeBy(500)
            runCurrent()

            assertEquals(2, cancellations)
            assertEquals(listOf(30), states.map { it.deviceVolume })
            subscription.cancel()
            coVerify(exactly = 3) { getDeviceVolume() }
            confirmVerified(getDeviceVolume)
        }

    private fun createFlow() =
        OtherVolumeDetailFlow(
            soundVolume = soundVolume,
            deviceVolumeRefreshTrigger = deviceVolumeRefreshTrigger,
            getDeviceVolume = { getDeviceVolume() },
            isPreviewing = isPreviewing,
            lowVolumeWarningEnabled = lowVolumeWarningEnabled,
        )
}
