package kurou.kodriver.feature.otherlist

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
            assertTrue(viewModel.uiState.first { it.showResetConfirmDialog }.showResetConfirmDialog)
            assertNull(viewModel.uiState.first().selectedItem)
            viewModel.onResetDismiss()
            assertFalse(viewModel.uiState.first { !it.showResetConfirmDialog }.showResetConfirmDialog)
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

            viewModel.onResetClick()
            viewModel.onResetConfirm()
            val state = viewModel.uiState.first { it.resetSucceeded != null && !it.isResetting }
            assertEquals(true, state.resetSucceeded)
            assertFalse(state.showResetConfirmDialog)
            viewModel.consumeResetResult()
            assertNull(viewModel.uiState.first { it.resetSucceeded == null }.resetSucceeded)
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
            val state = viewModel.uiState.first { it.resetSucceeded != null && !it.isResetting }
            assertEquals(false, state.resetSucceeded)
            assertFalse(state.showResetConfirmDialog)
            viewModel.onResetClick()
            assertTrue(viewModel.uiState.first { it.showResetConfirmDialog }.showResetConfirmDialog)
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
            assertTrue(viewModel.uiState.first { it.isResetting }.isResetting)
            viewModel.onResetClick()
            viewModel.onResetConfirm()
            assertFalse(viewModel.uiState.first().showResetConfirmDialog)
            completion.complete(Unit)
            assertEquals(true, viewModel.uiState.first { it.resetSucceeded != null && !it.isResetting }.resetSucceeded)
            Simulator.entries.forEach { simulator ->
                coVerify(exactly = 1) { telemetryLogRepository.deleteTelemetryLogsBySimulator(simulator) }
            }
            confirmVerified(telemetryLogRepository)
        }
}
