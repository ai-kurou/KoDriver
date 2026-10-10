@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherlist

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.READOUT_START_SOUND_TYPE_DEFAULT
import kurou.kodriver.domain.model.THEME_MODE_DEFAULT
import kurou.kodriver.domain.model.TextToSpeechUnavailableReason
import kurou.kodriver.domain.model.ThemeMode
import kurou.kodriver.domain.model.VOICE_SPEED_DEFAULT
import kurou.kodriver.domain.repository.AccessLocalNetworkPermissionRepository
import kurou.kodriver.domain.repository.AppUpdateRepository
import kurou.kodriver.domain.repository.ConsoleAddressPreferencesRepository
import kurou.kodriver.domain.repository.DeviceVolumeRepository
import kurou.kodriver.domain.repository.DynamicColorEnabledRepository
import kurou.kodriver.domain.repository.Gt7Ps5UdpPortPreferencesRepository
import kurou.kodriver.domain.repository.HapticFeedbackAvailabilityRepository
import kurou.kodriver.domain.repository.HapticFeedbackEnabledRepository
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository
import kurou.kodriver.domain.repository.OverlayVisiblePreferencesRepository
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository
import kurou.kodriver.domain.repository.ServerIpPreferencesRepository
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kurou.kodriver.domain.repository.SpeechSettingsSenderRepository
import kurou.kodriver.domain.repository.StartupEnabledRepository
import kurou.kodriver.domain.repository.TextToSpeechRepository
import kurou.kodriver.domain.repository.ThemePreferencesRepository
import kurou.kodriver.domain.repository.VoicePreferencesRepository
import kurou.kodriver.domain.repository.VoiceSpeedPreferencesRepository
import kurou.kodriver.domain.usecase.CheckAccessLocalNetworkPermissionGrantedUseCase
import kurou.kodriver.domain.usecase.CheckAppUpdateAvailableUseCase
import kurou.kodriver.domain.usecase.CheckHapticFeedbackAvailableUseCase
import kurou.kodriver.domain.usecase.CheckTextToSpeechUnavailableReasonUseCase
import kurou.kodriver.domain.usecase.GetDeviceVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveConsoleAddressUseCase
import kurou.kodriver.domain.usecase.ObserveDynamicColorEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UdpPortUseCase
import kurou.kodriver.domain.usecase.ObserveHapticFeedbackEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveKeepScreenOnEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveOverlayVisibleUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutStartSoundTypeUseCase
import kurou.kodriver.domain.usecase.ObserveServerIpUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveThemeModeUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceSpeedUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceUseCase
import kurou.kodriver.domain.usecase.OpenWindowsSpeechSettingsUseCase
import kurou.kodriver.domain.usecase.SaveDynamicColorEnabledUseCase
import kurou.kodriver.domain.usecase.SaveHapticFeedbackEnabledUseCase
import kurou.kodriver.domain.usecase.SaveKeepScreenOnEnabledUseCase
import kurou.kodriver.domain.usecase.SaveOverlayVisibleUseCase
import kurou.kodriver.domain.usecase.StartupRegistrationUseCases
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Android実機（SDK 31+）における buildOtherListItems() とのHapticFeedback項目・TTS案内項目の
 * フィルタリング連携を確認する。:core:data のプラットフォーム振り分けにより、jvmTestでは
 * HapticFeedback項目・TTS案内項目自体が定義されないため、ここでのみ検証する
 * （他のケースは [OtherListViewModelTest]（jvmTest）を参照）。
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OtherListViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    private val appUpdateRepository: AppUpdateRepository = mockk()

    private val overlayVisibleRepository: OverlayVisiblePreferencesRepository = mockk()

    private val keepScreenOnRepository: KeepScreenOnEnabledRepository = mockk()

    private val dynamicColorRepository: DynamicColorEnabledRepository = mockk()

    private val hapticFeedbackEnabledRepository: HapticFeedbackEnabledRepository = mockk()

    private val hapticFeedbackAvailabilityRepository: HapticFeedbackAvailabilityRepository = mockk()

    private val startupRegistrationRepository: StartupEnabledRepository = mockk()

    private val accessLocalNetworkPermissionRepository: AccessLocalNetworkPermissionRepository = mockk()

    private val textToSpeechRepository: TextToSpeechRepository = mockk()

    private val speechSettingsRepository: SpeechSettingsSenderRepository = mockk()

    private val readoutStartSoundRepository: ReadoutStartSoundPreferencesRepository = mockk()
    private val readoutStartSoundFlow = MutableStateFlow(READOUT_START_SOUND_TYPE_DEFAULT)

    private val consoleAddressRepository: ConsoleAddressPreferencesRepository = mockk()
    private val consolePortRepository: Gt7Ps5UdpPortPreferencesRepository = mockk()
    private val consoleAddressFlow = MutableStateFlow<String?>(null)
    private val consolePortFlow = MutableStateFlow(33740)

    private val serverIpRepository: ServerIpPreferencesRepository = mockk()
    private val serverIpFlow = MutableStateFlow<String?>(null)

    private val themeRepository: ThemePreferencesRepository = mockk()
    private val themeModeFlow = MutableStateFlow(THEME_MODE_DEFAULT)

    private val voiceRepository: VoicePreferencesRepository = mockk()
    private val voiceSpeedRepository: VoiceSpeedPreferencesRepository = mockk()
    private val voiceSpeedFlow = MutableStateFlow(VOICE_SPEED_DEFAULT)
    private val soundVolumeRepository: SoundVolumePreferencesRepository = mockk()
    private val deviceVolumeRepository: DeviceVolumeRepository = mockk()
    private val soundVolumeFlow = MutableStateFlow(80)

    private val voiceFlow = MutableStateFlow("")

    private val overlayVisibleFlow = MutableStateFlow(true)
    private val keepScreenOnFlow = MutableStateFlow(true)
    private val dynamicColorFlow = MutableStateFlow(false)
    private val hapticFeedbackFlow = MutableStateFlow(true)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { voiceSpeedRepository.voiceSpeed() } returns voiceSpeedFlow
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(hapticFeedbackAvailable: Boolean): OtherListViewModel {
        every { hapticFeedbackAvailabilityRepository.isHapticFeedbackAvailable() } returns hapticFeedbackAvailable
        every { accessLocalNetworkPermissionRepository.isGranted() } returns true
        return OtherListViewModel(
            checkAppUpdateAvailable = CheckAppUpdateAvailableUseCase(appUpdateRepository),
            settingsUseCases =
                OtherListSettingsUseCases(
                    observeOverlayVisible = ObserveOverlayVisibleUseCase(overlayVisibleRepository),
                    saveOverlayVisible = SaveOverlayVisibleUseCase(overlayVisibleRepository),
                    observeKeepScreenOn = ObserveKeepScreenOnEnabledUseCase(keepScreenOnRepository),
                    saveKeepScreenOn = SaveKeepScreenOnEnabledUseCase(keepScreenOnRepository),
                    observeDynamicColorEnabled = ObserveDynamicColorEnabledUseCase(dynamicColorRepository),
                    saveDynamicColorEnabled = SaveDynamicColorEnabledUseCase(dynamicColorRepository),
                    observeHapticFeedbackEnabled = ObserveHapticFeedbackEnabledUseCase(hapticFeedbackEnabledRepository),
                    saveHapticFeedbackEnabled = SaveHapticFeedbackEnabledUseCase(hapticFeedbackEnabledRepository),
                    observeVoice = ObserveVoiceUseCase(voiceRepository),
                    observeVoiceSpeed = ObserveVoiceSpeedUseCase(voiceSpeedRepository),
                    observeReadoutStartSoundType = ObserveReadoutStartSoundTypeUseCase(readoutStartSoundRepository),
                    observeSoundVolume = ObserveSoundVolumeUseCase(soundVolumeRepository),
                    observeThemeMode = ObserveThemeModeUseCase(themeRepository),
                    observeServerIp = ObserveServerIpUseCase(serverIpRepository),
                    observeConsoleAddress = ObserveConsoleAddressUseCase(consoleAddressRepository),
                    observeGt7Ps5UdpPort = ObserveGt7Ps5UdpPortUseCase(consolePortRepository),
                ),
            checkHapticFeedbackAvailable = CheckHapticFeedbackAvailableUseCase(hapticFeedbackAvailabilityRepository),
            checkAccessLocalNetworkPermissionGranted =
                CheckAccessLocalNetworkPermissionGrantedUseCase(accessLocalNetworkPermissionRepository),
            checkTextToSpeechUnavailableReason = CheckTextToSpeechUnavailableReasonUseCase(textToSpeechRepository),
            openWindowsSpeechSettings = OpenWindowsSpeechSettingsUseCase(speechSettingsRepository),
            startupRegistration = StartupRegistrationUseCases(startupRegistrationRepository),
            getDeviceVolume = GetDeviceVolumeUseCase(deviceVolumeRepository),
            appVersionInfo =
                OtherListAppVersionInfo(
                    currentVersion = "0.5.0",
                    appVersionLabel = "Androidアプリバージョン",
                ),
        )
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

    @Test
    fun `振動機能が利用可能な端末ではハプティックフィードバック項目が表示される`() =
        runTest {
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
            val viewModel = createViewModel(hapticFeedbackAvailable = true)

            assertTrue(
                viewModel.uiState
                    .first()
                    .items
                    .contains(OtherListItemType.HapticFeedback),
            )
            verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
            verify(exactly = 1) { dynamicColorRepository.dynamicColorEnabled() }
            verify(exactly = 1) { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() }
            verify(exactly = 1) { overlayVisibleRepository.observeOverlayVisible() }
            verify(exactly = 1) { hapticFeedbackAvailabilityRepository.isHapticFeedbackAvailable() }
            confirmVerified(
                appUpdateRepository,
                overlayVisibleRepository,
                keepScreenOnRepository,
                dynamicColorRepository,
                hapticFeedbackEnabledRepository,
                hapticFeedbackAvailabilityRepository,
            )
        }

    @Test
    fun `振動機能が利用不可な端末ではハプティックフィードバック項目が表示されない`() =
        runTest {
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
            val viewModel = createViewModel(hapticFeedbackAvailable = false)

            assertFalse(
                viewModel.uiState
                    .first()
                    .items
                    .contains(OtherListItemType.HapticFeedback),
            )
            verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
            verify(exactly = 1) { dynamicColorRepository.dynamicColorEnabled() }
            verify(exactly = 1) { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() }
            verify(exactly = 1) { overlayVisibleRepository.observeOverlayVisible() }
            verify(exactly = 1) { hapticFeedbackAvailabilityRepository.isHapticFeedbackAvailable() }
            confirmVerified(
                appUpdateRepository,
                overlayVisibleRepository,
                keepScreenOnRepository,
                dynamicColorRepository,
                hapticFeedbackEnabledRepository,
                hapticFeedbackAvailabilityRepository,
            )
        }

    @Test
    fun `初期状態ではTTS案内項目は表示されない`() =
        runTest {
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
            val viewModel = createViewModel(hapticFeedbackAvailable = true)

            val items = viewModel.uiState.first().items
            assertFalse(items.contains(OtherListItemType.TtsEngineMissing))
            assertFalse(items.contains(OtherListItemType.TtsLanguageDataMissing))
            verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
            verify(exactly = 1) { dynamicColorRepository.dynamicColorEnabled() }
            verify(exactly = 1) { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() }
            verify(exactly = 1) { overlayVisibleRepository.observeOverlayVisible() }
            verify(exactly = 1) { hapticFeedbackAvailabilityRepository.isHapticFeedbackAvailable() }
            confirmVerified(
                appUpdateRepository,
                overlayVisibleRepository,
                keepScreenOnRepository,
                dynamicColorRepository,
                hapticFeedbackEnabledRepository,
                hapticFeedbackAvailabilityRepository,
            )
        }

    @Test
    fun `checkTextToSpeechAvailabilityでエンジン未インストールと判明した場合はエンジンインストール案内のみ表示される`() =
        runTest {
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
            coEvery {
                textToSpeechRepository.unavailableReason()
            } returns TextToSpeechUnavailableReason.EngineMissing
            val viewModel = createViewModel(hapticFeedbackAvailable = true)

            viewModel.checkTextToSpeechAvailability()

            val state = viewModel.uiState.first()
            assertEquals(TtsUnavailableGuidance.EngineMissing, state.ttsUnavailableGuidance)
            val items = state.items
            assertTrue(items.contains(OtherListItemType.TtsEngineMissing))
            assertEquals(items.sortedBy { it.ordinal }, items)
            assertFalse(items.contains(OtherListItemType.TtsLanguageDataMissing))
            coVerify(exactly = 1) { textToSpeechRepository.unavailableReason() }
            verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
            verify(exactly = 1) { dynamicColorRepository.dynamicColorEnabled() }
            verify(exactly = 1) { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() }
            verify(exactly = 1) { overlayVisibleRepository.observeOverlayVisible() }
            verify(exactly = 1) { hapticFeedbackAvailabilityRepository.isHapticFeedbackAvailable() }
            confirmVerified(
                appUpdateRepository,
                overlayVisibleRepository,
                keepScreenOnRepository,
                dynamicColorRepository,
                hapticFeedbackEnabledRepository,
                hapticFeedbackAvailabilityRepository,
                textToSpeechRepository,
            )
        }

    @Test
    fun `checkTextToSpeechAvailabilityで言語データ未インストールと判明した場合は言語データ設定案内のみ表示される`() =
        runTest {
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
            coEvery {
                textToSpeechRepository.unavailableReason()
            } returns TextToSpeechUnavailableReason.LanguageDataMissing
            val viewModel = createViewModel(hapticFeedbackAvailable = true)

            viewModel.checkTextToSpeechAvailability()

            val state = viewModel.uiState.first()
            assertEquals(TtsUnavailableGuidance.LanguageDataMissing, state.ttsUnavailableGuidance)
            val items = state.items
            assertFalse(items.contains(OtherListItemType.TtsEngineMissing))
            assertTrue(items.contains(OtherListItemType.TtsLanguageDataMissing))
            coVerify(exactly = 1) { textToSpeechRepository.unavailableReason() }
            verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
            verify(exactly = 1) { dynamicColorRepository.dynamicColorEnabled() }
            verify(exactly = 1) { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() }
            verify(exactly = 1) { overlayVisibleRepository.observeOverlayVisible() }
            verify(exactly = 1) { hapticFeedbackAvailabilityRepository.isHapticFeedbackAvailable() }
            confirmVerified(
                appUpdateRepository,
                overlayVisibleRepository,
                keepScreenOnRepository,
                dynamicColorRepository,
                hapticFeedbackEnabledRepository,
                hapticFeedbackAvailabilityRepository,
                textToSpeechRepository,
            )
        }

    @Test
    fun `音声IDはシステム既定から保存済みIDの変更を反映する`() =
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

            voiceFlow.update { "saved-voice" }

            assertEquals("saved-voice", viewModel.uiState.first { it.voiceId == "saved-voice" }.voiceId)
        }

    @Test
    fun `読み上げ速度は既定値から保存済み速度の変更を反映する`() =
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
            assertEquals(VOICE_SPEED_DEFAULT, viewModel.uiState.first().voiceSpeed)

            voiceSpeedFlow.update { 1.5f }

            assertEquals(1.5f, viewModel.uiState.first { it.voiceSpeed == 1.5f }.voiceSpeed)
        }

    @Test
    fun `読み上げ速度をタップすると選択し再タップで解除する`() =
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
            viewModel.onItemSelected(OtherListItemType.VoiceSpeed)
            assertEquals(OtherListItemType.VoiceSpeed, viewModel.uiState.first().selectedItem)

            viewModel.onItemSelected(OtherListItemType.Volume)
            assertEquals(OtherListItemType.Volume, viewModel.uiState.first().selectedItem)
            viewModel.onItemSelected(OtherListItemType.VoiceSpeed)
            assertEquals(OtherListItemType.VoiceSpeed, viewModel.uiState.first().selectedItem)
            viewModel.onItemSelected(OtherListItemType.VoiceSpeed)
            assertNull(viewModel.uiState.first().selectedItem)
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

            themeModeFlow.update { ThemeMode.LIGHT }
            assertEquals(ThemeMode.LIGHT, viewModel.uiState.first { it.themeMode == ThemeMode.LIGHT }.themeMode)

            themeModeFlow.update { ThemeMode.SYSTEM }
            assertEquals(ThemeMode.SYSTEM, viewModel.uiState.first { it.themeMode == ThemeMode.SYSTEM }.themeMode)
            verify(exactly = 1) { themeRepository.observeThemeMode() }
            confirmVerified(themeRepository)
        }
}
