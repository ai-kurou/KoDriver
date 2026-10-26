package kurou.kodriver.feature.otherlist

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5UdpPortPreferencesRepository
import kurou.kodriver.domain.gt7ps5.usecase.ObserveGt7Ps5UdpPortUseCase
import kurou.kodriver.domain.model.ANDROID_AUDIO_USAGE_DEFAULT
import kurou.kodriver.domain.model.OVERLAY_BACKGROUND_OPACITY_DEFAULT
import kurou.kodriver.domain.model.OVERLAY_TEXT_COLOR_DEFAULT
import kurou.kodriver.domain.model.OVERLAY_TEXT_SIZE_DEFAULT
import kurou.kodriver.domain.model.READOUT_START_SOUND_TYPE_DEFAULT
import kurou.kodriver.domain.model.THEME_MODE_DEFAULT
import kurou.kodriver.domain.model.VOICE_PITCH_DEFAULT
import kurou.kodriver.domain.model.VOICE_SPEED_DEFAULT
import kurou.kodriver.domain.repository.AccessLocalNetworkPermissionRepository
import kurou.kodriver.domain.repository.AndroidAudioUsagePreferencesRepository
import kurou.kodriver.domain.repository.AppUpdateRepository
import kurou.kodriver.domain.repository.ConsoleAddressPreferencesRepository
import kurou.kodriver.domain.repository.DeviceVolumeRepository
import kurou.kodriver.domain.repository.DynamicColorEnabledRepository
import kurou.kodriver.domain.repository.HapticFeedbackAvailabilityRepository
import kurou.kodriver.domain.repository.HapticFeedbackEnabledRepository
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository
import kurou.kodriver.domain.repository.OverlayBackgroundOpacityPreferencesRepository
import kurou.kodriver.domain.repository.OverlayTextColorPreferencesRepository
import kurou.kodriver.domain.repository.OverlayTextSizePreferencesRepository
import kurou.kodriver.domain.repository.OverlayVisiblePreferencesRepository
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository
import kurou.kodriver.domain.repository.ServerIpPreferencesRepository
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kurou.kodriver.domain.repository.SpeechSettingsSenderRepository
import kurou.kodriver.domain.repository.StartupEnabledRepository
import kurou.kodriver.domain.repository.TelemetryLogRepository
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
import kurou.kodriver.domain.usecase.ObserveAndroidAudioUsageUseCase
import kurou.kodriver.domain.usecase.ObserveConsoleAddressUseCase
import kurou.kodriver.domain.usecase.ObserveDynamicColorEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveHapticFeedbackEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveKeepScreenOnEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveOverlayBackgroundOpacityUseCase
import kurou.kodriver.domain.usecase.ObserveOverlayTextColorUseCase
import kurou.kodriver.domain.usecase.ObserveOverlayTextSizeUseCase
import kurou.kodriver.domain.usecase.ObserveOverlayVisibleUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutStartSoundTypeUseCase
import kurou.kodriver.domain.usecase.ObserveServerIpUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveThemeModeUseCase
import kurou.kodriver.domain.usecase.ObserveVoicePitchUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceSpeedUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceUseCase
import kurou.kodriver.domain.usecase.OpenWindowsSpeechSettingsUseCase
import kurou.kodriver.domain.usecase.ResetTelemetryLogDatabaseUseCase
import kurou.kodriver.domain.usecase.SaveDynamicColorEnabledUseCase
import kurou.kodriver.domain.usecase.SaveHapticFeedbackEnabledUseCase
import kurou.kodriver.domain.usecase.SaveKeepScreenOnEnabledUseCase
import kurou.kodriver.domain.usecase.SaveOverlayVisibleUseCase
import kurou.kodriver.domain.usecase.StartupRegistrationUseCases
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

/**
 * 分割したテスト間で Repository のモック、Flow、UseCase の構築を共有する。
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("LargeClass")
abstract class OtherListViewModelTestFixture {
    protected val telemetryLogRepository: TelemetryLogRepository = mockk()

    protected val dispatcher = UnconfinedTestDispatcher()

    protected val appUpdateRepository: AppUpdateRepository = mockk()

    protected val overlayTextSizeRepository: OverlayTextSizePreferencesRepository = mockk()
    protected val overlayBackgroundOpacityRepository: OverlayBackgroundOpacityPreferencesRepository = mockk()
    protected val overlayTextColorRepository: OverlayTextColorPreferencesRepository = mockk()
    protected val overlayTextSizeFlow = MutableStateFlow(OVERLAY_TEXT_SIZE_DEFAULT)
    protected val overlayBackgroundOpacityFlow = MutableStateFlow(OVERLAY_BACKGROUND_OPACITY_DEFAULT)
    protected val overlayTextColorFlow = MutableStateFlow(OVERLAY_TEXT_COLOR_DEFAULT)

    protected val overlayVisibleRepository: OverlayVisiblePreferencesRepository = mockk()

    protected val keepScreenOnRepository: KeepScreenOnEnabledRepository = mockk()

    protected val dynamicColorRepository: DynamicColorEnabledRepository = mockk()

    protected val hapticFeedbackEnabledRepository: HapticFeedbackEnabledRepository = mockk()

    protected val hapticFeedbackAvailabilityRepository: HapticFeedbackAvailabilityRepository = mockk()

    protected val startupRegistrationRepository: StartupEnabledRepository = mockk()

    protected val accessLocalNetworkPermissionRepository: AccessLocalNetworkPermissionRepository = mockk()

    protected val textToSpeechRepository: TextToSpeechRepository = mockk()

    protected val speechSettingsRepository: SpeechSettingsSenderRepository = mockk()

    protected val readoutStartSoundRepository: ReadoutStartSoundPreferencesRepository = mockk()
    protected val readoutStartSoundFlow = MutableStateFlow(READOUT_START_SOUND_TYPE_DEFAULT)

    protected val consoleAddressRepository: ConsoleAddressPreferencesRepository = mockk()
    protected val consolePortRepository: Gt7Ps5UdpPortPreferencesRepository = mockk()
    protected val consoleAddressFlow = MutableStateFlow<String?>(null)
    protected val consolePortFlow = MutableStateFlow(33740)

    protected val serverIpRepository: ServerIpPreferencesRepository = mockk()
    protected val serverIpFlow = MutableStateFlow<String?>(null)

    protected val themeRepository: ThemePreferencesRepository = mockk()
    protected val themeModeFlow = MutableStateFlow(THEME_MODE_DEFAULT)

    protected val voiceRepository: VoicePreferencesRepository = mockk()
    protected val voiceSpeedRepository: VoiceSpeedPreferencesRepository = mockk()
    protected val voicePitchRepository: VoicePitchPreferencesRepository = mockk()
    protected val voiceSpeedFlow = MutableStateFlow(VOICE_SPEED_DEFAULT)
    protected val audioUsageRepository: AndroidAudioUsagePreferencesRepository = mockk()
    protected val audioUsageFlow = MutableStateFlow(ANDROID_AUDIO_USAGE_DEFAULT)

    protected val voicePitchFlow = MutableStateFlow(VOICE_PITCH_DEFAULT)
    protected val soundVolumeRepository: SoundVolumePreferencesRepository = mockk()
    protected val deviceVolumeRepository: DeviceVolumeRepository = mockk()
    protected val soundVolumeFlow = MutableStateFlow(80)

    protected val voiceFlow = MutableStateFlow("")

    protected val overlayVisibleFlow = MutableStateFlow(true)
    protected val keepScreenOnFlow = MutableStateFlow(true)
    protected val dynamicColorFlow = MutableStateFlow(false)
    protected val hapticFeedbackFlow = MutableStateFlow(true)

    @BeforeTest
    open fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { voiceSpeedRepository.voiceSpeed() } returns voiceSpeedFlow
        every { voicePitchRepository.voicePitch() } returns voicePitchFlow
        every { audioUsageRepository.observeAndroidAudioUsage() } returns audioUsageFlow
        every { overlayTextColorRepository.observeOverlayTextColor() } returns overlayTextColorFlow
        every { overlayTextSizeRepository.observeOverlayTextSize() } returns overlayTextSizeFlow
        every {
            overlayBackgroundOpacityRepository.observeOverlayBackgroundOpacity()
        } returns overlayBackgroundOpacityFlow
    }

    @AfterTest
    open fun tearDown() {
        Dispatchers.resetMain()
    }

    protected fun createViewModel(
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
            overlayUseCases =
                OtherListOverlayUseCases(
                    observeOverlayVisible = ObserveOverlayVisibleUseCase(overlayVisibleRepository),
                    saveOverlayVisible = SaveOverlayVisibleUseCase(overlayVisibleRepository),
                    observeOverlayTextSize = ObserveOverlayTextSizeUseCase(overlayTextSizeRepository),
                    observeOverlayTextColor = ObserveOverlayTextColorUseCase(overlayTextColorRepository),
                    observeOverlayBackgroundOpacity =
                        ObserveOverlayBackgroundOpacityUseCase(overlayBackgroundOpacityRepository),
                ),
            speechUseCases =
                OtherListSpeechUseCases(
                    observeVoice = ObserveVoiceUseCase(voiceRepository),
                    observeVoiceSpeed = ObserveVoiceSpeedUseCase(voiceSpeedRepository),
                    observeVoicePitch = ObserveVoicePitchUseCase(voicePitchRepository),
                    observeAndroidAudioUsage = ObserveAndroidAudioUsageUseCase(audioUsageRepository),
                    observeReadoutStartSoundType = ObserveReadoutStartSoundTypeUseCase(readoutStartSoundRepository),
                    observeSoundVolume = ObserveSoundVolumeUseCase(soundVolumeRepository),
                ),
            deviceSettingsUseCases =
                OtherListDeviceSettingsUseCases(
                    observeKeepScreenOn = ObserveKeepScreenOnEnabledUseCase(keepScreenOnRepository),
                    saveKeepScreenOn = SaveKeepScreenOnEnabledUseCase(keepScreenOnRepository),
                    observeDynamicColorEnabled = ObserveDynamicColorEnabledUseCase(dynamicColorRepository),
                    saveDynamicColorEnabled = SaveDynamicColorEnabledUseCase(dynamicColorRepository),
                    observeHapticFeedbackEnabled = ObserveHapticFeedbackEnabledUseCase(hapticFeedbackEnabledRepository),
                    saveHapticFeedbackEnabled = SaveHapticFeedbackEnabledUseCase(hapticFeedbackEnabledRepository),
                    observeThemeMode = ObserveThemeModeUseCase(themeRepository),
                ),
            connectionUseCases =
                OtherListConnectionUseCases(
                    observeServerIp = if (observeServerIp) ObserveServerIpUseCase(serverIpRepository) else null,
                    observeConsoleAddress = ObserveConsoleAddressUseCase(consoleAddressRepository),
                    observeGt7Ps5UdpPort = ObserveGt7Ps5UdpPortUseCase(consolePortRepository),
                ),
            checkHapticFeedbackAvailable = CheckHapticFeedbackAvailableUseCase(hapticFeedbackAvailabilityRepository),
            checkAccessLocalNetworkPermissionGranted =
                CheckAccessLocalNetworkPermissionGrantedUseCase(accessLocalNetworkPermissionRepository),
            checkTextToSpeechUnavailableReason = CheckTextToSpeechUnavailableReasonUseCase(textToSpeechRepository),
            openWindowsSpeechSettings = OpenWindowsSpeechSettingsUseCase(speechSettingsRepository),
            startupRegistrationFlow =
                OtherListStartupRegistrationFlow(StartupRegistrationUseCases(startupRegistrationRepository)),
            getDeviceVolume = GetDeviceVolumeUseCase(deviceVolumeRepository),
            resetFlow = OtherListResetFlow(ResetTelemetryLogDatabaseUseCase(telemetryLogRepository)),
            appVersionInfo =
                OtherListAppVersionInfo(
                    currentVersion = currentVersion,
                    appVersionLabel = "Windows版KoDriverバージョン",
                ),
        )
    }
}
