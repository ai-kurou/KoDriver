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
class OtherListViewModelAutoReadoutSettingsResetTest : OtherListViewModelTestFixture() {
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

            viewModel.onAutoReadoutSettingsResetClick()
            val shownState = viewModel.uiState.first { it.autoReadoutSettingsReset.showResetConfirmDialog }
            assertTrue(shownState.autoReadoutSettingsReset.showResetConfirmDialog)
            assertNull(viewModel.uiState.first().selectedItem)
            viewModel.onAutoReadoutSettingsResetDismiss()
            val dismissedState = viewModel.uiState.first { !it.autoReadoutSettingsReset.showResetConfirmDialog }
            assertFalse(dismissedState.autoReadoutSettingsReset.showResetConfirmDialog)
            viewModel.onAutoReadoutSettingsResetConfirm()
            coVerify(exactly = 0) { autoReadoutSettingsResetter.resetToDefault() }
            confirmVerified(autoReadoutSettingsResetter)
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
            coEvery { autoReadoutSettingsResetter.resetToDefault() } returns Unit
            val viewModel = createViewModel()

            viewModel.selectFeedbackItem(42L)
            val selectedState = viewModel.uiState.first { it.selectedFeedbackTelemetryLogId == 42L }
            viewModel.onAutoReadoutSettingsResetClick()
            viewModel.onAutoReadoutSettingsResetConfirm()
            val state =
                viewModel.uiState.first {
                    it.autoReadoutSettingsReset.resetSucceeded != null && !it.autoReadoutSettingsReset.isResetting
                }
            assertEquals(selectedState.copy(autoReadoutSettingsReset = state.autoReadoutSettingsReset), state)
            assertEquals(true, state.autoReadoutSettingsReset.resetSucceeded)
            assertFalse(state.autoReadoutSettingsReset.showResetConfirmDialog)
            viewModel.consumeAutoReadoutSettingsResetResult()
            val consumedState = viewModel.uiState.first { it.autoReadoutSettingsReset.resetSucceeded == null }
            assertEquals(selectedState, consumedState)
            coVerify(exactly = 1) { autoReadoutSettingsResetter.resetToDefault() }
            confirmVerified(autoReadoutSettingsResetter)
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
            coEvery { autoReadoutSettingsResetter.resetToDefault() } throws
                IllegalStateException("初期化に失敗しました")
            val viewModel = createViewModel()

            viewModel.onAutoReadoutSettingsResetClick()
            viewModel.onAutoReadoutSettingsResetConfirm()
            val state =
                viewModel.uiState.first {
                    it.autoReadoutSettingsReset.resetSucceeded != null && !it.autoReadoutSettingsReset.isResetting
                }
            assertEquals(false, state.autoReadoutSettingsReset.resetSucceeded)
            assertFalse(state.autoReadoutSettingsReset.showResetConfirmDialog)
            viewModel.onAutoReadoutSettingsResetClick()
            val shownState = viewModel.uiState.first { it.autoReadoutSettingsReset.showResetConfirmDialog }
            assertTrue(shownState.autoReadoutSettingsReset.showResetConfirmDialog)
            coVerify(exactly = 1) { autoReadoutSettingsResetter.resetToDefault() }
            confirmVerified(autoReadoutSettingsResetter)
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
            coEvery { autoReadoutSettingsResetter.resetToDefault() } coAnswers {
                completion.await()
            }
            val viewModel = createViewModel()

            viewModel.onAutoReadoutSettingsResetClick()
            viewModel.onAutoReadoutSettingsResetConfirm()
            val resettingState = viewModel.uiState.first { it.autoReadoutSettingsReset.isResetting }
            assertTrue(resettingState.autoReadoutSettingsReset.isResetting)
            viewModel.onAutoReadoutSettingsResetClick()
            viewModel.onAutoReadoutSettingsResetConfirm()
            val stateWhileResetting = viewModel.uiState.first()
            assertFalse(stateWhileResetting.autoReadoutSettingsReset.showResetConfirmDialog)
            completion.complete(Unit)
            val resultState =
                viewModel.uiState.first {
                    it.autoReadoutSettingsReset.resetSucceeded != null && !it.autoReadoutSettingsReset.isResetting
                }
            assertEquals(true, resultState.autoReadoutSettingsReset.resetSucceeded)
            coVerify(exactly = 1) { autoReadoutSettingsResetter.resetToDefault() }
            confirmVerified(autoReadoutSettingsResetter)
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
            coEvery { autoReadoutSettingsResetter.resetToDefault() } coAnswers {
                try {
                    completion.await()
                } finally {
                    cancelled.complete(Unit)
                }
            }
            val viewModel = createViewModel()
            val store = ViewModelStore()
            store.put("otherList", viewModel)
            viewModel.onAutoReadoutSettingsResetClick()
            viewModel.onAutoReadoutSettingsResetConfirm()
            val resettingState = viewModel.uiState.first { it.autoReadoutSettingsReset.isResetting }
            assertTrue(resettingState.autoReadoutSettingsReset.isResetting)

            store.clear()

            assertTrue(cancelled.isCompleted)
            completion.complete(Unit)
            coVerify(exactly = 1) {
                autoReadoutSettingsResetter.resetToDefault()
            }
            confirmVerified(autoReadoutSettingsResetter)
        }
}
