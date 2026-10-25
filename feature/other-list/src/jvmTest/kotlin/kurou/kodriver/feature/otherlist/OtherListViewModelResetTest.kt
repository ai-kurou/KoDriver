package kurou.kodriver.feature.otherlist

import androidx.lifecycle.ViewModelStore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OtherListViewModelResetTest : OtherListViewModelTestFixture() {
    @Test
    fun `確認ダイアログを表示してキャンセルすると削除しない`() =
        runTest(dispatcher) {
            every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOnFlow
            every { dynamicColorRepository.dynamicColorEnabled() } returns dynamicColorFlow
            every { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() } returns hapticFeedbackFlow
            every { soundVolumeRepository.volume() } returns soundVolumeFlow
            coEvery { deviceVolumeRepository.getVolume() } returns 60
            every { readoutStartSoundRepository.observeType() } returns readoutStartSoundFlow
            every { voiceRepository.voiceId() } returns voiceFlow
            every { themeRepository.observeThemeMode() } returns themeModeFlow
            every { serverIpRepository.serverIp() } returns serverIpFlow
            every { consoleAddressRepository.consoleAddress() } returns consoleAddressFlow
            every { consolePortRepository.port() } returns consolePortFlow
            every { overlayVisibleRepository.observeOverlayVisible() } returns overlayVisibleFlow
            val viewModel = createViewModel()

            viewModel.onResetClick()
            assertTrue(viewModel.uiState.first { it.reset.showResetConfirmDialog }.reset.showResetConfirmDialog)
            assertNull(viewModel.uiState.first().selectedItem)
            viewModel.onResetDismiss()
            assertFalse(viewModel.uiState.first { !it.reset.showResetConfirmDialog }.reset.showResetConfirmDialog)
            viewModel.onResetConfirm()
            Simulator.entries.forEach { simulator ->
                coVerify(exactly = 0) { telemetryLogRepository.deleteTelemetryLogsBySimulator(simulator) }
            }
            confirmVerified(telemetryLogRepository)
        }

    @Test
    fun `確認すると全シミュレーターを削除して結果を消費できる`() =
        runTest(dispatcher) {
            every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOnFlow
            every { dynamicColorRepository.dynamicColorEnabled() } returns dynamicColorFlow
            every { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() } returns hapticFeedbackFlow
            every { soundVolumeRepository.volume() } returns soundVolumeFlow
            coEvery { deviceVolumeRepository.getVolume() } returns 60
            every { readoutStartSoundRepository.observeType() } returns readoutStartSoundFlow
            every { voiceRepository.voiceId() } returns voiceFlow
            every { themeRepository.observeThemeMode() } returns themeModeFlow
            every { serverIpRepository.serverIp() } returns serverIpFlow
            every { consoleAddressRepository.consoleAddress() } returns consoleAddressFlow
            every { consolePortRepository.port() } returns consolePortFlow
            every { overlayVisibleRepository.observeOverlayVisible() } returns overlayVisibleFlow
            Simulator.entries.forEach { simulator ->
                coEvery { telemetryLogRepository.deleteTelemetryLogsBySimulator(simulator) } returns Unit
            }
            val viewModel = createViewModel()

            viewModel.selectFeedbackItem(42L)
            val selectedState = viewModel.uiState.first { it.selectedFeedbackTelemetryLogId == 42L }
            viewModel.onResetClick()
            viewModel.onResetConfirm()
            val state = viewModel.uiState.first { it.reset.resetSucceeded != null && !it.reset.isResetting }
            assertEquals(selectedState.copy(reset = state.reset), state)
            assertEquals(true, state.reset.resetSucceeded)
            assertFalse(state.reset.showResetConfirmDialog)
            viewModel.consumeResetResult()
            val consumedState = viewModel.uiState.first { it.reset.resetSucceeded == null }
            assertEquals(selectedState, consumedState)
            Simulator.entries.forEach { simulator ->
                coVerify(exactly = 1) { telemetryLogRepository.deleteTelemetryLogsBySimulator(simulator) }
            }
            confirmVerified(telemetryLogRepository)
        }

    @Test
    fun `削除失敗を通知して再試行できる`() =
        runTest(dispatcher) {
            every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOnFlow
            every { dynamicColorRepository.dynamicColorEnabled() } returns dynamicColorFlow
            every { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() } returns hapticFeedbackFlow
            every { soundVolumeRepository.volume() } returns soundVolumeFlow
            coEvery { deviceVolumeRepository.getVolume() } returns 60
            every { readoutStartSoundRepository.observeType() } returns readoutStartSoundFlow
            every { voiceRepository.voiceId() } returns voiceFlow
            every { themeRepository.observeThemeMode() } returns themeModeFlow
            every { serverIpRepository.serverIp() } returns serverIpFlow
            every { consoleAddressRepository.consoleAddress() } returns consoleAddressFlow
            every { consolePortRepository.port() } returns consolePortFlow
            every { overlayVisibleRepository.observeOverlayVisible() } returns overlayVisibleFlow
            val firstSimulator = Simulator.entries.first()
            coEvery { telemetryLogRepository.deleteTelemetryLogsBySimulator(firstSimulator) } throws
                IllegalStateException("削除に失敗しました")
            val viewModel = createViewModel()

            viewModel.onResetClick()
            viewModel.onResetConfirm()
            val state = viewModel.uiState.first { it.reset.resetSucceeded != null && !it.reset.isResetting }
            assertEquals(false, state.reset.resetSucceeded)
            assertFalse(state.reset.showResetConfirmDialog)
            viewModel.onResetClick()
            assertTrue(viewModel.uiState.first { it.reset.showResetConfirmDialog }.reset.showResetConfirmDialog)
            coVerify(exactly = 1) { telemetryLogRepository.deleteTelemetryLogsBySimulator(firstSimulator) }
            confirmVerified(telemetryLogRepository)
        }

    @Test
    fun `削除中の再タップと再確認では二重実行しない`() =
        runTest(dispatcher) {
            every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOnFlow
            every { dynamicColorRepository.dynamicColorEnabled() } returns dynamicColorFlow
            every { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() } returns hapticFeedbackFlow
            every { soundVolumeRepository.volume() } returns soundVolumeFlow
            coEvery { deviceVolumeRepository.getVolume() } returns 60
            every { readoutStartSoundRepository.observeType() } returns readoutStartSoundFlow
            every { voiceRepository.voiceId() } returns voiceFlow
            every { themeRepository.observeThemeMode() } returns themeModeFlow
            every { serverIpRepository.serverIp() } returns serverIpFlow
            every { consoleAddressRepository.consoleAddress() } returns consoleAddressFlow
            every { consolePortRepository.port() } returns consolePortFlow
            every { overlayVisibleRepository.observeOverlayVisible() } returns overlayVisibleFlow
            val completion = CompletableDeferred<Unit>()
            Simulator.entries.forEach { simulator ->
                coEvery { telemetryLogRepository.deleteTelemetryLogsBySimulator(simulator) } coAnswers {
                    completion.await()
                }
            }
            val viewModel = createViewModel()

            viewModel.onResetClick()
            viewModel.onResetConfirm()
            assertTrue(viewModel.uiState.first { it.reset.isResetting }.reset.isResetting)
            viewModel.onResetClick()
            viewModel.onResetConfirm()
            assertFalse(viewModel.uiState.first().reset.showResetConfirmDialog)
            completion.complete(Unit)
            val resultState = viewModel.uiState.first { it.reset.resetSucceeded != null && !it.reset.isResetting }
            assertEquals(true, resultState.reset.resetSucceeded)
            Simulator.entries.forEach { simulator ->
                coVerify(exactly = 1) { telemetryLogRepository.deleteTelemetryLogsBySimulator(simulator) }
            }
            confirmVerified(telemetryLogRepository)
        }

    @Test
    fun `ViewModel破棄で削除処理をキャンセルする`() =
        runTest(dispatcher) {
            every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOnFlow
            every { dynamicColorRepository.dynamicColorEnabled() } returns dynamicColorFlow
            every { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() } returns hapticFeedbackFlow
            every { soundVolumeRepository.volume() } returns soundVolumeFlow
            coEvery { deviceVolumeRepository.getVolume() } returns 60
            every { readoutStartSoundRepository.observeType() } returns readoutStartSoundFlow
            every { voiceRepository.voiceId() } returns voiceFlow
            every { themeRepository.observeThemeMode() } returns themeModeFlow
            every { serverIpRepository.serverIp() } returns serverIpFlow
            every { consoleAddressRepository.consoleAddress() } returns consoleAddressFlow
            every { consolePortRepository.port() } returns consolePortFlow
            every { overlayVisibleRepository.observeOverlayVisible() } returns overlayVisibleFlow
            val firstSimulator = Simulator.entries.first()
            val completion = CompletableDeferred<Unit>()
            val cancelled = CompletableDeferred<Unit>()
            coEvery { telemetryLogRepository.deleteTelemetryLogsBySimulator(firstSimulator) } coAnswers {
                try {
                    completion.await()
                } finally {
                    cancelled.complete(Unit)
                }
            }
            val viewModel = createViewModel()
            val store = ViewModelStore()
            store.put("otherList", viewModel)
            viewModel.onResetClick()
            viewModel.onResetConfirm()
            assertTrue(viewModel.uiState.first { it.reset.isResetting }.reset.isResetting)

            store.clear()

            assertTrue(cancelled.isCompleted)
            completion.complete(Unit)
            Simulator.entries.forEach { simulator ->
                coVerify(exactly = if (simulator == firstSimulator) 1 else 0) {
                    telemetryLogRepository.deleteTelemetryLogsBySimulator(simulator)
                }
            }
            confirmVerified(telemetryLogRepository)
        }
}
