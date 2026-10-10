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
import kurou.kodriver.domain.model.AppUpdate
import kurou.kodriver.domain.model.READOUT_START_SOUND_TYPE_DEFAULT
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.model.THEME_MODE_DEFAULT
import kurou.kodriver.domain.model.TextToSpeechUnavailableReason
import kurou.kodriver.domain.model.ThemeMode
import kurou.kodriver.domain.model.VOICE_PITCH_DEFAULT
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
import kurou.kodriver.domain.repository.VoicePitchPreferencesRepository
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
import kurou.kodriver.domain.usecase.ObserveVoicePitchUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceSpeedUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceUseCase
import kurou.kodriver.domain.usecase.OpenWindowsSpeechSettingsUseCase
import kurou.kodriver.domain.usecase.SaveDynamicColorEnabledUseCase
import kurou.kodriver.domain.usecase.SaveHapticFeedbackEnabledUseCase
import kurou.kodriver.domain.usecase.SaveKeepScreenOnEnabledUseCase
import kurou.kodriver.domain.usecase.SaveOverlayVisibleUseCase
import kurou.kodriver.domain.usecase.StartupRegistrationUseCases
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("TooManyFunctions", "LargeClass")
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
    private val voicePitchRepository: VoicePitchPreferencesRepository = mockk()
    private val voiceSpeedFlow = MutableStateFlow(VOICE_SPEED_DEFAULT)
    private val voicePitchFlow = MutableStateFlow(VOICE_PITCH_DEFAULT)
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
        every { voicePitchRepository.voicePitch() } returns voicePitchFlow
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        currentVersion: String = "0.5.0",
        observeServerIp: Boolean = true,
        hapticFeedbackAvailable: Boolean = true,
        accessLocalNetworkPermissionGranted: Boolean = true,
    ): OtherListViewModel {
        every { hapticFeedbackAvailabilityRepository.isHapticFeedbackAvailable() } returns hapticFeedbackAvailable
        every {
            accessLocalNetworkPermissionRepository.isGranted()
        } returns accessLocalNetworkPermissionGranted
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
                    observeVoicePitch = ObserveVoicePitchUseCase(voicePitchRepository),
                    observeReadoutStartSoundType = ObserveReadoutStartSoundTypeUseCase(readoutStartSoundRepository),
                    observeSoundVolume = ObserveSoundVolumeUseCase(soundVolumeRepository),
                    observeThemeMode = ObserveThemeModeUseCase(themeRepository),
                    observeServerIp = if (observeServerIp) ObserveServerIpUseCase(serverIpRepository) else null,
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
                    currentVersion = currentVersion,
                    appVersionLabel = "Windows版KoDriverバージョン",
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
        val viewModel = createViewModel()

        viewModel.openWindowsSpeechSettings()

        verify(exactly = 1) { speechSettingsRepository.openWindowsSpeechSettings() }
        confirmVerified(speechSettingsRepository)
    }

    @Test
    fun `初期状態では全項目が表示され選択項目はない`() =
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
            val viewModel = createViewModel()

            assertEquals(buildOtherListItems(), viewModel.uiState.first().items)
            assertEquals("Windows版KoDriverバージョン", viewModel.uiState.first().appVersionLabel)
            assertEquals("0.5.0", viewModel.uiState.first().appVersion)
            assertNull(viewModel.uiState.first().selectedItem)
            assertNull(viewModel.uiState.first().ttsUnavailableGuidance)
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
    fun `音量を選択すると選択状態になる`() =
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
            val viewModel = createViewModel()

            viewModel.onItemSelected(OtherListItemType.Volume)

            assertEquals(OtherListItemType.Volume, viewModel.uiState.first().selectedItem)
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
    fun `GitHubレポジトリまたはリリースページまたはローカルネットワークへのアクセス許可を選択しても状態は変わらない`() =
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
            val viewModel = createViewModel()
            val initialState = viewModel.uiState.first()

            viewModel.onItemSelected(OtherListItemType.GitHubRepository)
            viewModel.onItemSelected(OtherListItemType.ReleasePage)
            viewModel.onItemSelected(OtherListItemType.AccessLocalNetworkPermission)

            assertEquals(initialState, viewModel.uiState.first())
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
    fun `onItemSelectedで項目を選択し再選択すると解除される`() =
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
            val viewModel = createViewModel()

            viewModel.onItemSelected(OtherListItemType.License)
            assertEquals(OtherListItemType.License, viewModel.uiState.first().selectedItem)

            viewModel.onItemSelected(OtherListItemType.License)
            assertNull(viewModel.uiState.first().selectedItem)
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
    fun `selectItemで同じ項目を連続して選択しても選択状態が維持される`() =
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
            val viewModel = createViewModel()

            viewModel.selectItem(OtherListItemType.ConsoleIp)
            assertEquals(OtherListItemType.ConsoleIp, viewModel.uiState.first().selectedItem)

            viewModel.selectItem(OtherListItemType.ConsoleIp)
            assertEquals(OtherListItemType.ConsoleIp, viewModel.uiState.first().selectedItem)
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
    fun `selectFeedbackItemでフィードバック項目が選択されテレメトリログIDが保持される`() =
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
            val viewModel = createViewModel()

            viewModel.selectFeedbackItem(42L)
            val firstRequestId = viewModel.uiState.first().feedbackAttachRequestId

            viewModel.selectFeedbackItem(42L)

            assertEquals(OtherListItemType.Feedback, viewModel.uiState.first().selectedItem)
            assertEquals(42L, viewModel.uiState.first().selectedFeedbackTelemetryLogId)
            assertEquals(firstRequestId + 1, viewModel.uiState.first().feedbackAttachRequestId)
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
    fun `selectFeedbackItemの後にonItemSelectedで別項目を選択するとテレメトリログIDが解除される`() =
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
            val viewModel = createViewModel()

            viewModel.selectFeedbackItem(42L)
            viewModel.onItemSelected(OtherListItemType.Volume)

            assertNull(viewModel.uiState.first().selectedFeedbackTelemetryLogId)
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
    fun `clearSelectedItemで選択状態が解除される`() =
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
            val viewModel = createViewModel()

            viewModel.onItemSelected(OtherListItemType.License)
            viewModel.clearSelectedItem()

            assertNull(viewModel.uiState.first().selectedItem)
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
    fun `オーバーレイ表示の状態を監視できる`() =
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
            val viewModel = createViewModel()

            overlayVisibleFlow.update { false }

            assertFalse(viewModel.uiState.first().overlayVisible)
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
    fun `onOverlayVisibleChangeでオーバーレイ表示の状態を保存できる`() =
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
                overlayVisibleRepository.saveOverlayVisible(false)
            } answers { overlayVisibleFlow.update { false } }
            val viewModel = createViewModel()

            viewModel.onOverlayVisibleChange(false)

            assertFalse(overlayVisibleFlow.first())
            assertFalse(viewModel.uiState.first().overlayVisible)
            verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
            verify(exactly = 1) { dynamicColorRepository.dynamicColorEnabled() }
            verify(exactly = 1) { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() }
            verify(exactly = 1) { overlayVisibleRepository.observeOverlayVisible() }
            verify(exactly = 1) { hapticFeedbackAvailabilityRepository.isHapticFeedbackAvailable() }
            coVerify(exactly = 1) { overlayVisibleRepository.saveOverlayVisible(false) }
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
    fun `画面スリープ無効の状態を監視できる`() =
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
            val viewModel = createViewModel()

            keepScreenOnFlow.update { false }

            assertEquals(false, viewModel.uiState.first().keepScreenOn)
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
    fun `onKeepScreenOnChangeで画面スリープ無効の状態を保存できる`() =
        runTest {
            every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOnFlow
            coEvery { keepScreenOnRepository.saveKeepScreenOn(false) } answers { keepScreenOnFlow.update { false } }
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

            viewModel.onKeepScreenOnChange(false)

            assertEquals(false, keepScreenOnFlow.first())
            assertEquals(false, viewModel.uiState.first().keepScreenOn)
            verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
            verify(exactly = 1) { dynamicColorRepository.dynamicColorEnabled() }
            verify(exactly = 1) { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() }
            verify(exactly = 1) { overlayVisibleRepository.observeOverlayVisible() }
            verify(exactly = 1) { hapticFeedbackAvailabilityRepository.isHapticFeedbackAvailable() }
            coVerify(exactly = 1) { keepScreenOnRepository.saveKeepScreenOn(false) }
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
    fun `Dynamic Colorの有効状態を監視・保存できる`() =
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
            coEvery { dynamicColorRepository.saveDynamicColorEnabled(true) } answers {
                dynamicColorFlow.update { true }
            }
            val viewModel = createViewModel()

            viewModel.onDynamicColorEnabledChange(true)

            assertEquals(true, dynamicColorFlow.first())
            assertEquals(true, viewModel.uiState.first().dynamicColorEnabled)
            verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
            verify(exactly = 1) { dynamicColorRepository.dynamicColorEnabled() }
            verify(exactly = 1) { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() }
            verify(exactly = 1) { overlayVisibleRepository.observeOverlayVisible() }
            verify(exactly = 1) { hapticFeedbackAvailabilityRepository.isHapticFeedbackAvailable() }
            coVerify(exactly = 1) { dynamicColorRepository.saveDynamicColorEnabled(true) }
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
    fun `ハプティックフィードバックの有効状態を監視・保存できる`() =
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
            coEvery { hapticFeedbackEnabledRepository.saveHapticFeedbackEnabled(false) } answers {
                hapticFeedbackFlow.update { false }
            }
            val viewModel = createViewModel()

            viewModel.onHapticFeedbackEnabledChange(false)

            assertEquals(false, hapticFeedbackFlow.first())
            assertEquals(false, viewModel.uiState.first().hapticFeedbackEnabled)
            verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
            verify(exactly = 1) { dynamicColorRepository.dynamicColorEnabled() }
            verify(exactly = 1) { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() }
            verify(exactly = 1) { overlayVisibleRepository.observeOverlayVisible() }
            verify(exactly = 1) { hapticFeedbackAvailabilityRepository.isHapticFeedbackAvailable() }
            coVerify(exactly = 1) { hapticFeedbackEnabledRepository.saveHapticFeedbackEnabled(false) }
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
    fun `振動機能が利用不可な場合ハプティックフィードバック項目が表示されない`() =
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
    fun `最新バージョンがある場合hasAppUpdateがtrueになる`() =
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
            coEvery { appUpdateRepository.getLatestRelease() } returns AppUpdate(tagName = "v9.9.9")
            val viewModel = createViewModel(currentVersion = "1.0.0")

            viewModel.checkUpdate()

            assertTrue(viewModel.uiState.first().hasAppUpdate)
            coVerify(exactly = 1) { appUpdateRepository.getLatestRelease() }
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
    fun `現在が最新バージョンの場合hasAppUpdateがfalseになる`() =
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
            coEvery { appUpdateRepository.getLatestRelease() } returns AppUpdate(tagName = "v1.0.0")
            val viewModel = createViewModel(currentVersion = "1.0.0")

            viewModel.checkUpdate()

            assertFalse(viewModel.uiState.first().hasAppUpdate)
            coVerify(exactly = 1) { appUpdateRepository.getLatestRelease() }
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
    fun `checkUpdateを呼ぶ前はhasAppUpdateがfalseのまま`() =
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
            val viewModel = createViewModel(currentVersion = "1.0.0")

            assertFalse(viewModel.uiState.first().hasAppUpdate)
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
    fun `currentVersionが空の場合checkUpdateは何もしない`() =
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
            val viewModel = createViewModel(currentVersion = "")

            viewModel.checkUpdate()

            assertFalse(viewModel.uiState.first().hasAppUpdate)
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
    fun `リリース情報がnullの場合hasAppUpdateがfalseになる`() =
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
            coEvery { appUpdateRepository.getLatestRelease() } returns null
            val viewModel = createViewModel(currentVersion = "1.0.0")

            viewModel.checkUpdate()

            assertFalse(viewModel.uiState.first().hasAppUpdate)
            coVerify(exactly = 1) { appUpdateRepository.getLatestRelease() }
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
    fun `checkStartupEnabledでOS起動時自動起動の状態を取得できる`() =
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
            coEvery { startupRegistrationRepository.isEnabled() } returns true
            val viewModel = createViewModel()

            viewModel.checkStartupEnabled()

            assertTrue(viewModel.uiState.first().startupEnabled)
            coVerify(exactly = 1) { startupRegistrationRepository.isEnabled() }
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
                startupRegistrationRepository,
            )
        }

    @Test
    fun `onStartupEnabledChangeでOS起動時自動起動の状態を保存できる`() =
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
            coEvery { startupRegistrationRepository.setEnabled(true) } returns Unit
            val viewModel = createViewModel()

            viewModel.onStartupEnabledChange(true)

            assertTrue(viewModel.uiState.first().startupEnabled)
            coVerify(exactly = 1) { startupRegistrationRepository.setEnabled(true) }
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
                startupRegistrationRepository,
            )
        }

    @Test
    fun `初期状態で権限が許可済みの場合accessLocalNetworkPermissionGrantedがtrueになる`() =
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
            val viewModel = createViewModel(accessLocalNetworkPermissionGranted = true)

            assertTrue(viewModel.uiState.first().accessLocalNetworkPermissionGranted)
            verify(exactly = 1) { accessLocalNetworkPermissionRepository.isGranted() }
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
                accessLocalNetworkPermissionRepository,
            )
        }

    @Test
    fun `初期状態で権限が未許可の場合accessLocalNetworkPermissionGrantedがfalseになる`() =
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
            val viewModel = createViewModel(accessLocalNetworkPermissionGranted = false)

            assertFalse(viewModel.uiState.first().accessLocalNetworkPermissionGranted)
            verify(exactly = 1) { accessLocalNetworkPermissionRepository.isGranted() }
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
                accessLocalNetworkPermissionRepository,
            )
        }

    @Test
    fun `checkAccessLocalNetworkPermissionで権限状態を再取得できる`() =
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
            val viewModel = createViewModel(accessLocalNetworkPermissionGranted = false)
            every { accessLocalNetworkPermissionRepository.isGranted() } returns true

            viewModel.checkAccessLocalNetworkPermission()

            assertTrue(viewModel.uiState.first().accessLocalNetworkPermissionGranted)
            verify(exactly = 2) { accessLocalNetworkPermissionRepository.isGranted() }
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
                accessLocalNetworkPermissionRepository,
            )
        }

    @Test
    fun `checkTextToSpeechAvailabilityでTTSが利用可能ならTTS案内項目は表示されない`() =
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
            coEvery { textToSpeechRepository.unavailableReason() } returns null
            val viewModel = createViewModel()

            viewModel.checkTextToSpeechAvailability()

            val state = viewModel.uiState.first()
            assertEquals(null, state.ttsUnavailableGuidance)
            val items = state.items
            assertFalse(items.contains(OtherListItemType.TtsEngineMissing))
            assertFalse(items.contains(OtherListItemType.TtsLanguageDataMissing))
            assertFalse(items.contains(OtherListItemType.WindowsSpeechUnavailable))
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
    fun `checkTextToSpeechAvailabilityでWindows音声が利用できない場合はWindows向け案内を表示する`() =
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
            coEvery { textToSpeechRepository.unavailableReason() } returns
                TextToSpeechUnavailableReason.WindowsSpeechUnavailable
            val viewModel = createViewModel()

            viewModel.checkTextToSpeechAvailability()

            val state = viewModel.uiState.first()
            assertEquals(TtsUnavailableGuidance.WindowsSpeechUnavailable, state.ttsUnavailableGuidance)
            val items = state.items
            assertTrue(items.contains(OtherListItemType.WindowsSpeechUnavailable))
            assertEquals(items.sortedBy { it.ordinal }, items)
            assertFalse(items.contains(OtherListItemType.TtsEngineMissing))
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
    fun `checkTextToSpeechAvailabilityでEngineMissingをUiStateに公開する`() =
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
            coEvery { textToSpeechRepository.unavailableReason() } returns
                TextToSpeechUnavailableReason.EngineMissing
            val viewModel = createViewModel()

            viewModel.checkTextToSpeechAvailability()

            val state = viewModel.uiState.first()
            assertEquals(TtsUnavailableGuidance.EngineMissing, state.ttsUnavailableGuidance)
            val items = state.items
            assertTrue(items.contains(OtherListItemType.TtsEngineMissing))
            assertEquals(items.sortedBy { it.ordinal }, items)
            assertFalse(items.contains(OtherListItemType.WindowsSpeechUnavailable))
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
    fun `checkTextToSpeechAvailabilityでLanguageDataMissingをUiStateに公開する`() =
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
            coEvery { textToSpeechRepository.unavailableReason() } returns
                TextToSpeechUnavailableReason.LanguageDataMissing
            val viewModel = createViewModel()

            viewModel.checkTextToSpeechAvailability()

            val state = viewModel.uiState.first()
            assertEquals(TtsUnavailableGuidance.LanguageDataMissing, state.ttsUnavailableGuidance)
            val items = state.items
            assertTrue(items.contains(OtherListItemType.TtsLanguageDataMissing))
            assertEquals(items.sortedBy { it.ordinal }, items)
            assertFalse(items.contains(OtherListItemType.TtsEngineMissing))
            assertFalse(items.contains(OtherListItemType.WindowsSpeechUnavailable))
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

            themeModeFlow.update { ThemeMode.LIGHT }
            assertEquals(ThemeMode.LIGHT, viewModel.uiState.first { it.themeMode == ThemeMode.LIGHT }.themeMode)

            themeModeFlow.update { ThemeMode.SYSTEM }
            assertEquals(ThemeMode.SYSTEM, viewModel.uiState.first { it.themeMode == ThemeMode.SYSTEM }.themeMode)
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
            val viewModel = createViewModel()
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
            val viewModel = createViewModel()
            assertEquals(VOICE_SPEED_DEFAULT, viewModel.uiState.first().voiceSpeed)

            voiceSpeedFlow.update { 1.5f }

            assertEquals(1.5f, viewModel.uiState.first { it.voiceSpeed == 1.5f }.voiceSpeed)
        }

    @Test
    fun `声の高さは既定値から保存済みピッチの変更を反映する`() =
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
            assertEquals(VOICE_PITCH_DEFAULT, viewModel.uiState.first().voicePitch)

            voicePitchFlow.update { 1.5f }

            assertEquals(1.5f, viewModel.uiState.first { it.voicePitch == 1.5f }.voicePitch)
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
            val viewModel = createViewModel()
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
    fun `声の高さをタップすると選択し再タップで解除する`() =
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
            viewModel.onItemSelected(OtherListItemType.VoicePitch)
            assertEquals(OtherListItemType.VoicePitch, viewModel.uiState.first().selectedItem)

            viewModel.onItemSelected(OtherListItemType.Volume)
            assertEquals(OtherListItemType.Volume, viewModel.uiState.first().selectedItem)
            viewModel.onItemSelected(OtherListItemType.VoicePitch)
            assertEquals(OtherListItemType.VoicePitch, viewModel.uiState.first().selectedItem)
            viewModel.onItemSelected(OtherListItemType.VoicePitch)
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
}
