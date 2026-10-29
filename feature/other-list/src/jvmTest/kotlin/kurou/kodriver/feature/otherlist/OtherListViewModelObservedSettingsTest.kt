package kurou.kodriver.feature.otherlist

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ANDROID_AUDIO_USAGE_DEFAULT
import kurou.kodriver.domain.model.AndroidAudioUsage
import kurou.kodriver.domain.model.OVERLAY_BACKGROUND_OPACITY_DEFAULT
import kurou.kodriver.domain.model.OVERLAY_TEXT_COLOR_DEFAULT
import kurou.kodriver.domain.model.OVERLAY_TEXT_SIZE_DEFAULT
import kurou.kodriver.domain.model.OverlayTextSize
import kurou.kodriver.domain.model.READOUT_START_SOUND_TYPE_DEFAULT
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.model.THEME_MODE_DEFAULT
import kurou.kodriver.domain.model.ThemeMode
import kurou.kodriver.domain.model.VOICE_PITCH_DEFAULT
import kurou.kodriver.domain.model.VOICE_SPEED_DEFAULT
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class OtherListViewModelObservedSettingsTest : OtherListViewModelTestFixture() {
    @Test
    fun `オーバーレイ表示は保存済み設定とその変更を反映する`() =
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
            val viewModel = createViewModel()
            assertEquals(true, viewModel.uiState.first().overlayVisible)

            overlayVisibleFlow.update { false }

            assertEquals(false, viewModel.uiState.first { !it.overlayVisible }.overlayVisible)
            verify(exactly = 1) { overlayVisibleRepository.observeOverlayVisible() }
            confirmVerified(overlayVisibleRepository)
        }

    @Test
    fun `ゲーム機の保存済みIPとポートおよび変更を反映する`() =
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
            consoleAddressFlow.update { "192.168.1.100" }
            consolePortFlow.update { 33741 }
            val viewModel = createViewModel()
            val saved = viewModel.uiState.first { it.consoleAddress == "192.168.1.100" && it.consolePort == 33741 }
            assertEquals("192.168.1.100", saved.consoleAddress)
            assertEquals(33741, saved.consolePort)
            consoleAddressFlow.update { "192.168.1.101" }
            assertEquals(
                "192.168.1.101",
                viewModel.uiState.first { it.consoleAddress == "192.168.1.101" }.consoleAddress,
            )
            consolePortFlow.update { 33740 }
            assertEquals(33740, viewModel.uiState.first { it.consolePort == 33740 }.consolePort)
            consoleAddressFlow.update { null }
            assertEquals(null, viewModel.uiState.first { it.consoleAddress == null }.consoleAddress)
            verify(exactly = 1) { consoleAddressRepository.consoleAddress() }
            verify(exactly = 1) { consolePortRepository.port() }
            confirmVerified(consoleAddressRepository, consolePortRepository)
        }

    @Test
    fun `接続先IPは未設定と保存済み設定とその変更を反映する`() =
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
            val viewModel = createViewModel()
            assertEquals(null, viewModel.uiState.first().serverIp)
            serverIpFlow.update { "192.168.1.100" }
            assertEquals("192.168.1.100", viewModel.uiState.first { it.serverIp != null }.serverIp)
            serverIpFlow.update { "192.168.1.101" }
            assertEquals("192.168.1.101", viewModel.uiState.first { it.serverIp == "192.168.1.101" }.serverIp)
            serverIpFlow.update { null }
            assertEquals(null, viewModel.uiState.first { it.serverIp == null }.serverIp)
            verify(exactly = 1) { serverIpRepository.serverIp() }
            confirmVerified(serverIpRepository)
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
            val viewModel = createViewModel(observeServerIp = false)
            assertEquals(THEME_MODE_DEFAULT, viewModel.uiState.value.themeMode)
            assertEquals(ThemeMode.DARK, viewModel.uiState.first { it.themeMode == ThemeMode.DARK }.themeMode)

            listOf(ThemeMode.LIGHT, ThemeMode.SYSTEM).forEach { themeMode ->
                themeModeFlow.update { themeMode }
                assertEquals(themeMode, viewModel.uiState.first { it.themeMode == themeMode }.themeMode)
            }
            verify(exactly = 1) { themeRepository.observeThemeMode() }
            assertEquals(null, viewModel.uiState.first().serverIp)
            verify(exactly = 0) { serverIpRepository.serverIp() }
            confirmVerified(themeRepository, serverIpRepository)
        }

    @Test
    fun `開始音種別は保存済み設定とその変更を反映する`() =
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
            readoutStartSoundFlow.update { ReadoutStartSoundType.ELECTRONIC_NOISE }
            val viewModel = createViewModel()
            assertEquals(READOUT_START_SOUND_TYPE_DEFAULT, viewModel.uiState.value.readoutStartSoundType)
            assertEquals(
                ReadoutStartSoundType.ELECTRONIC_NOISE,
                viewModel.uiState
                    .first {
                        it.readoutStartSoundType == ReadoutStartSoundType.ELECTRONIC_NOISE
                    }.readoutStartSoundType,
            )

            readoutStartSoundFlow.update { ReadoutStartSoundType.FORMULA_RADIO }

            assertEquals(
                ReadoutStartSoundType.FORMULA_RADIO,
                viewModel.uiState
                    .first {
                        it.readoutStartSoundType == ReadoutStartSoundType.FORMULA_RADIO
                    }.readoutStartSoundType,
            )
        }

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
            val viewModel = createViewModel()
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
    fun `音声の用途は既定値から保存済み設定の変更を反映する`() =
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
            val viewModel = createViewModel()
            assertEquals(ANDROID_AUDIO_USAGE_DEFAULT, viewModel.uiState.first().androidAudioUsage)

            audioUsageFlow.update { AndroidAudioUsage.ALARM }

            assertEquals(
                AndroidAudioUsage.ALARM,
                viewModel.uiState.first { it.androidAudioUsage == AndroidAudioUsage.ALARM }.androidAudioUsage,
            )
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
            val viewModel = createViewModel()
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
    fun `オーバーレイ文字色は既定値から保存済み設定の変更を反映する`() =
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
            val viewModel = createViewModel()
            assertEquals(OVERLAY_TEXT_COLOR_DEFAULT, viewModel.uiState.first().overlayTextColor)

            overlayTextColorFlow.update { 0x80123456L }

            assertEquals(0x80123456L, viewModel.uiState.first { it.overlayTextColor == 0x80123456L }.overlayTextColor)
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
            val viewModel = createViewModel()
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
    fun `音量の変更と端末音量の定期取得を反映し購読終了後は取得を停止する`() =
        runTest {
            every { soundVolumeRepository.volume() } returns soundVolumeFlow
            var deviceVolume = 60
            coEvery { deviceVolumeRepository.getVolume() } answers { deviceVolume }
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
            coVerify(exactly = 0) { deviceVolumeRepository.getVolume() }
            val subscription =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    viewModel.uiState.collect()
                }
            runCurrent()
            val initial = viewModel.uiState.first { it.soundVolume == 80 && it.deviceVolume == 60 }
            assertEquals(80, initial.soundVolume)
            assertEquals(60, initial.deviceVolume)

            soundVolumeFlow.update { 100 }
            assertEquals(100, viewModel.uiState.first { it.soundVolume == 100 }.soundVolume)
            deviceVolume = 0
            advanceTimeBy(1_999)
            assertEquals(60, viewModel.uiState.first().deviceVolume)
            advanceTimeBy(1)
            runCurrent()
            assertEquals(0, viewModel.uiState.first { it.deviceVolume == 0 }.deviceVolume)

            subscription.cancel()
            runCurrent()
            advanceTimeBy(5_000)
            runCurrent()
            // 購読中の2回と、購読終了後の5秒の猶予中の2回。猶予が終了すると停止する。
            coVerify(exactly = 4) { deviceVolumeRepository.getVolume() }
            advanceTimeBy(1_000)
            runCurrent()
            coVerify(exactly = 4) { deviceVolumeRepository.getVolume() }
            confirmVerified(deviceVolumeRepository)
        }

    @Test
    fun `端末音量の取得に失敗しても直前の値を維持して取得を続ける`() =
        runTest(dispatcher) {
            every { soundVolumeRepository.volume() } returns MutableStateFlow(80)
            var attempt = 0
            coEvery { deviceVolumeRepository.getVolume() } answers {
                when (attempt++) {
                    0 -> throw IllegalStateException("no audio endpoint")
                    1 -> 60
                    else -> throw IllegalStateException("device removed")
                }
            }
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
            val subscription =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    viewModel.uiState.collect()
                }
            runCurrent()

            assertEquals(0, viewModel.uiState.first { it.soundVolume == 80 }.deviceVolume)
            advanceTimeBy(2_000)
            runCurrent()
            assertEquals(60, viewModel.uiState.first { it.deviceVolume == 60 }.deviceVolume)
            advanceTimeBy(2_000)
            runCurrent()
            assertEquals(60, viewModel.uiState.first().deviceVolume)

            subscription.cancel()
            coVerify(atLeast = 3) { deviceVolumeRepository.getVolume() }
            confirmVerified(deviceVolumeRepository)
        }
}
