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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OtherListViewModelManualReadoutSettingsResetTest : OtherListViewModelTestFixture() {
    @Test
    fun `確認ダイアログを表示してキャンセルすると初期化しない`() =
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

            viewModel.onManualReadoutSettingsResetClick()
            val shownState = viewModel.uiState.first { it.manualReadoutSettingsReset.showResetConfirmDialog }
            assertTrue(shownState.manualReadoutSettingsReset.showResetConfirmDialog)
            assertNull(viewModel.uiState.first().selectedItem)
            viewModel.onManualReadoutSettingsResetDismiss()
            val dismissedState = viewModel.uiState.first { !it.manualReadoutSettingsReset.showResetConfirmDialog }
            assertFalse(dismissedState.manualReadoutSettingsReset.showResetConfirmDialog)
            viewModel.onManualReadoutSettingsResetConfirm()
            coVerify(exactly = 0) { manualReadoutSettingsResetter.resetToDefault() }
            confirmVerified(manualReadoutSettingsResetter)
        }

    @Test
    fun `確認すると全シミュレーターを初期化して結果を消費できる`() =
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
            coEvery { manualReadoutSettingsResetter.resetToDefault() } returns Unit
            val viewModel = createViewModel()

            viewModel.selectFeedbackItem(42L)
            val selectedState = viewModel.uiState.first { it.selectedFeedbackTelemetryLogId == 42L }
            viewModel.onManualReadoutSettingsResetClick()
            viewModel.onManualReadoutSettingsResetConfirm()
            val state =
                viewModel.uiState.first {
                    it.manualReadoutSettingsReset.resetSucceeded != null && !it.manualReadoutSettingsReset.isResetting
                }
            assertEquals(selectedState.copy(manualReadoutSettingsReset = state.manualReadoutSettingsReset), state)
            assertEquals(true, state.manualReadoutSettingsReset.resetSucceeded)
            assertFalse(state.manualReadoutSettingsReset.showResetConfirmDialog)
            viewModel.consumeManualReadoutSettingsResetResult()
            val consumedState = viewModel.uiState.first { it.manualReadoutSettingsReset.resetSucceeded == null }
            assertEquals(selectedState, consumedState)
            coVerify(exactly = 1) { manualReadoutSettingsResetter.resetToDefault() }
            confirmVerified(manualReadoutSettingsResetter)
        }

    @Test
    fun `初期化失敗を通知して再試行できる`() =
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
            coEvery { manualReadoutSettingsResetter.resetToDefault() } throws
                IllegalStateException("初期化に失敗しました")
            val viewModel = createViewModel()

            viewModel.onManualReadoutSettingsResetClick()
            viewModel.onManualReadoutSettingsResetConfirm()
            val state =
                viewModel.uiState.first {
                    it.manualReadoutSettingsReset.resetSucceeded != null && !it.manualReadoutSettingsReset.isResetting
                }
            assertEquals(false, state.manualReadoutSettingsReset.resetSucceeded)
            assertFalse(state.manualReadoutSettingsReset.showResetConfirmDialog)
            viewModel.onManualReadoutSettingsResetClick()
            val shownState = viewModel.uiState.first { it.manualReadoutSettingsReset.showResetConfirmDialog }
            assertTrue(shownState.manualReadoutSettingsReset.showResetConfirmDialog)
            coVerify(exactly = 1) { manualReadoutSettingsResetter.resetToDefault() }
            confirmVerified(manualReadoutSettingsResetter)
        }

    @Test
    fun `初期化中の再タップと再確認では二重実行しない`() =
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
            coEvery { manualReadoutSettingsResetter.resetToDefault() } coAnswers {
                completion.await()
            }
            val viewModel = createViewModel()

            viewModel.onManualReadoutSettingsResetClick()
            viewModel.onManualReadoutSettingsResetConfirm()
            val resettingState = viewModel.uiState.first { it.manualReadoutSettingsReset.isResetting }
            assertTrue(resettingState.manualReadoutSettingsReset.isResetting)
            viewModel.onManualReadoutSettingsResetClick()
            viewModel.onManualReadoutSettingsResetConfirm()
            val stateWhileResetting = viewModel.uiState.first()
            assertFalse(stateWhileResetting.manualReadoutSettingsReset.showResetConfirmDialog)
            completion.complete(Unit)
            val resultState =
                viewModel.uiState.first {
                    it.manualReadoutSettingsReset.resetSucceeded != null && !it.manualReadoutSettingsReset.isResetting
                }
            assertEquals(true, resultState.manualReadoutSettingsReset.resetSucceeded)
            coVerify(exactly = 1) { manualReadoutSettingsResetter.resetToDefault() }
            confirmVerified(manualReadoutSettingsResetter)
        }

    @Test
    fun `ViewModel破棄で初期化処理をキャンセルする`() =
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
            val cancelled = CompletableDeferred<Unit>()
            coEvery { manualReadoutSettingsResetter.resetToDefault() } coAnswers {
                try {
                    completion.await()
                } finally {
                    cancelled.complete(Unit)
                }
            }
            val viewModel = createViewModel()
            val store = ViewModelStore()
            store.put("otherList", viewModel)
            viewModel.onManualReadoutSettingsResetClick()
            viewModel.onManualReadoutSettingsResetConfirm()
            val resettingState = viewModel.uiState.first { it.manualReadoutSettingsReset.isResetting }
            assertTrue(resettingState.manualReadoutSettingsReset.isResetting)

            store.clear()

            assertTrue(cancelled.isCompleted)
            completion.complete(Unit)
            coVerify(exactly = 1) {
                manualReadoutSettingsResetter.resetToDefault()
            }
            confirmVerified(manualReadoutSettingsResetter)
        }
}
