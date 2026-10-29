@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherlist

import io.mockk.coEvery
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.OVERLAY_BACKGROUND_OPACITY_DEFAULT
import kurou.kodriver.domain.model.OVERLAY_TEXT_SIZE_DEFAULT
import kurou.kodriver.domain.model.OverlayTextSize
import kurou.kodriver.domain.model.THEME_MODE_DEFAULT
import kurou.kodriver.domain.model.ThemeMode
import kurou.kodriver.domain.model.VOICE_PITCH_DEFAULT
import kurou.kodriver.domain.model.VOICE_SPEED_DEFAULT
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OtherListViewModelSettingsTest : OtherListViewModelTestFixture() {
    @Test
    fun `音声IDと読み上げ速度と声の高さは既定値から保存済み設定の変更を反映する`() =
        runTest {
            every { soundVolumeRepository.volume() } returns soundVolumeFlow
            coEvery { deviceVolumeRepository.getVolume() } returns 60
            every { readoutStartSoundRepository.observeType() } returns readoutStartSoundFlow
            every { voiceRepository.voiceId() } returns voiceFlow
            every { themeRepository.observeThemeMode() } returns themeModeFlow
            every { serverIpRepository.serverIp() } returns serverIpFlow
            every { consoleAddressRepository.consoleAddress() } returns consoleAddressFlow
            every { consolePortRepository.port() } returns consolePortFlow
            every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOnFlow
            every { dynamicColorRepository.dynamicColorEnabled() } returns dynamicColorFlow
            every { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() } returns hapticFeedbackFlow
            every { overlayVisibleRepository.observeOverlayVisible() } returns overlayVisibleFlow
            val viewModel = createViewModel(hapticFeedbackAvailable = true)
            assertEquals("", viewModel.uiState.first().voiceId)
            assertEquals(VOICE_SPEED_DEFAULT, viewModel.uiState.first().voiceSpeed)
            assertEquals(VOICE_PITCH_DEFAULT, viewModel.uiState.first().voicePitch)

            voiceFlow.update { "saved-voice" }

            assertEquals("saved-voice", viewModel.uiState.first { it.voiceId == "saved-voice" }.voiceId)

            voiceSpeedFlow.update { 1.5f }

            assertEquals(1.5f, viewModel.uiState.first { it.voiceSpeed == 1.5f }.voiceSpeed)

            voicePitchFlow.update { 1.5f }

            assertEquals(1.5f, viewModel.uiState.first { it.voicePitch == 1.5f }.voicePitch)
        }

    @Test
    fun `オーバーレイ文字サイズと背景透明度は既定値から保存済み設定の変更を反映する`() =
        runTest {
            every { soundVolumeRepository.volume() } returns soundVolumeFlow
            coEvery { deviceVolumeRepository.getVolume() } returns 60
            every { readoutStartSoundRepository.observeType() } returns readoutStartSoundFlow
            every { voiceRepository.voiceId() } returns voiceFlow
            every { themeRepository.observeThemeMode() } returns themeModeFlow
            every { serverIpRepository.serverIp() } returns serverIpFlow
            every { consoleAddressRepository.consoleAddress() } returns consoleAddressFlow
            every { consolePortRepository.port() } returns consolePortFlow
            every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOnFlow
            every { dynamicColorRepository.dynamicColorEnabled() } returns dynamicColorFlow
            every { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() } returns hapticFeedbackFlow
            every { overlayVisibleRepository.observeOverlayVisible() } returns overlayVisibleFlow
            val viewModel = createViewModel(hapticFeedbackAvailable = true)
            assertEquals(OVERLAY_TEXT_SIZE_DEFAULT, viewModel.uiState.first().overlayTextSize)
            assertEquals(OVERLAY_BACKGROUND_OPACITY_DEFAULT, viewModel.uiState.first().overlayBackgroundOpacity)

            overlayTextSizeFlow.update { OverlayTextSize.LARGE }

            assertEquals(
                OverlayTextSize.LARGE,
                viewModel.uiState.first { it.overlayTextSize == OverlayTextSize.LARGE }.overlayTextSize,
            )

            overlayBackgroundOpacityFlow.update { 75 }

            assertEquals(75, viewModel.uiState.first { it.overlayBackgroundOpacity == 75 }.overlayBackgroundOpacity)
        }

    @Test
    fun `読み上げ速度と声の高さをタップすると選択し再タップで解除する`() =
        runTest {
            every { soundVolumeRepository.volume() } returns soundVolumeFlow
            coEvery { deviceVolumeRepository.getVolume() } returns 60
            every { readoutStartSoundRepository.observeType() } returns readoutStartSoundFlow
            every { voiceRepository.voiceId() } returns voiceFlow
            every { themeRepository.observeThemeMode() } returns themeModeFlow
            every { serverIpRepository.serverIp() } returns serverIpFlow
            every { consoleAddressRepository.consoleAddress() } returns consoleAddressFlow
            every { consolePortRepository.port() } returns consolePortFlow
            every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOnFlow
            every { dynamicColorRepository.dynamicColorEnabled() } returns dynamicColorFlow
            every { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() } returns hapticFeedbackFlow
            every { overlayVisibleRepository.observeOverlayVisible() } returns overlayVisibleFlow
            val viewModel = createViewModel(hapticFeedbackAvailable = true)
            listOf(OtherListItemType.VoiceSpeed, OtherListItemType.VoicePitch).forEach { itemType ->
                viewModel.onItemSelected(itemType)
                assertEquals(itemType, viewModel.uiState.first().selectedItem)

                viewModel.onItemSelected(OtherListItemType.Volume)
                assertEquals(OtherListItemType.Volume, viewModel.uiState.first().selectedItem)
                viewModel.onItemSelected(itemType)
                assertEquals(itemType, viewModel.uiState.first().selectedItem)
                viewModel.onItemSelected(itemType)
                assertNull(viewModel.uiState.first().selectedItem)
            }
        }

    @Test
    fun `テーマは保存済み設定とその変更を反映する`() =
        runTest {
            every { soundVolumeRepository.volume() } returns soundVolumeFlow
            coEvery { deviceVolumeRepository.getVolume() } returns 60
            every { readoutStartSoundRepository.observeType() } returns readoutStartSoundFlow
            every { voiceRepository.voiceId() } returns voiceFlow
            every { themeRepository.observeThemeMode() } returns themeModeFlow
            every { serverIpRepository.serverIp() } returns serverIpFlow
            every { consoleAddressRepository.consoleAddress() } returns consoleAddressFlow
            every { consolePortRepository.port() } returns consolePortFlow
            every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOnFlow
            every { dynamicColorRepository.dynamicColorEnabled() } returns dynamicColorFlow
            every { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() } returns hapticFeedbackFlow
            every { overlayVisibleRepository.observeOverlayVisible() } returns overlayVisibleFlow
            themeModeFlow.update { ThemeMode.DARK }
            val viewModel = createViewModel(hapticFeedbackAvailable = true)
            assertEquals(THEME_MODE_DEFAULT, viewModel.uiState.value.themeMode)
            assertEquals(ThemeMode.DARK, viewModel.uiState.first { it.themeMode == ThemeMode.DARK }.themeMode)

            listOf(ThemeMode.LIGHT, ThemeMode.SYSTEM).forEach { themeMode ->
                themeModeFlow.update { themeMode }
                assertEquals(themeMode, viewModel.uiState.first { it.themeMode == themeMode }.themeMode)
            }
            verify(exactly = 1) { themeRepository.observeThemeMode() }
            confirmVerified(themeRepository)
        }

    @Test
    fun `Windowsの音声設定を開く処理をUseCase経由でRepositoryへ委譲する`() {
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
        every { speechSettingsRepository.openWindowsSpeechSettings() } returns Unit
        val viewModel = createViewModel(hapticFeedbackAvailable = true)

        viewModel.openWindowsSpeechSettings()

        verify(exactly = 1) { speechSettingsRepository.openWindowsSpeechSettings() }
        confirmVerified(speechSettingsRepository)
    }
}
