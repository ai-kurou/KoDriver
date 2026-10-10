package kurou.kodriver.feature.otherlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.DEVICE_VOLUME_MIN
import kurou.kodriver.domain.model.TextToSpeechUnavailableReason
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

/**
 * アプリバージョン表示に必要な情報（現在バージョン・プラットフォーム別ラベル）。
 */
data class OtherListAppVersionInfo(
    val currentVersion: String,
    val appVersionLabel: String,
)

data class OtherListSettingsUseCases(
    val observeOverlayVisible: ObserveOverlayVisibleUseCase,
    val saveOverlayVisible: SaveOverlayVisibleUseCase,
    val observeKeepScreenOn: ObserveKeepScreenOnEnabledUseCase,
    val saveKeepScreenOn: SaveKeepScreenOnEnabledUseCase,
    val observeDynamicColorEnabled: ObserveDynamicColorEnabledUseCase,
    val saveDynamicColorEnabled: SaveDynamicColorEnabledUseCase,
    val observeHapticFeedbackEnabled: ObserveHapticFeedbackEnabledUseCase,
    val saveHapticFeedbackEnabled: SaveHapticFeedbackEnabledUseCase,
    val observeVoice: ObserveVoiceUseCase,
    val observeVoiceSpeed: ObserveVoiceSpeedUseCase,
    val observeVoicePitch: ObserveVoicePitchUseCase,
    val observeReadoutStartSoundType: ObserveReadoutStartSoundTypeUseCase,
    val observeSoundVolume: ObserveSoundVolumeUseCase,
    val observeThemeMode: ObserveThemeModeUseCase,
    val observeServerIp: ObserveServerIpUseCase?,
    val observeConsoleAddress: ObserveConsoleAddressUseCase,
    val observeGt7Ps5UdpPort: ObserveGt7Ps5UdpPortUseCase,
)

/**
 * OtherList 画面の状態管理とユーザー操作を扱う ViewModel。
 */
@Suppress("LongParameterList")
class OtherListViewModel(
    private val checkAppUpdateAvailable: CheckAppUpdateAvailableUseCase,
    private val settingsUseCases: OtherListSettingsUseCases,
    checkHapticFeedbackAvailable: CheckHapticFeedbackAvailableUseCase,
    private val checkAccessLocalNetworkPermissionGranted: CheckAccessLocalNetworkPermissionGrantedUseCase,
    private val checkTextToSpeechUnavailableReason: CheckTextToSpeechUnavailableReasonUseCase,
    private val openWindowsSpeechSettings: OpenWindowsSpeechSettingsUseCase,
    private val startupRegistration: StartupRegistrationUseCases,
    appVersionInfo: OtherListAppVersionInfo,
    private val getDeviceVolume: GetDeviceVolumeUseCase,
) : ViewModel() {
    private val currentVersion = appVersionInfo.currentVersion
    private val hapticFeedbackAvailable = checkHapticFeedbackAvailable()
    private val textToSpeechUnavailableReason = MutableStateFlow<TextToSpeechUnavailableReason?>(null)
    private val _uiState =
        MutableStateFlow(
            OtherListUiState(
                appVersionLabel = appVersionInfo.appVersionLabel,
                appVersion = appVersionInfo.currentVersion,
                items =
                    buildOtherListItems().filterNot {
                        it == OtherListItemType.HapticFeedback && !hapticFeedbackAvailable
                    },
                accessLocalNetworkPermissionGranted = checkAccessLocalNetworkPermissionGranted(),
            ),
        )

    // 端末のマスター音量はOSへの問い合わせが必要なため、購読中だけ一定間隔で取得する。
    // 既定の出力デバイスがない場合などで取得に失敗しても一覧全体を止めないよう、
    // 失敗時は直前の値（初回は最小値）を維持する。
    private val deviceVolumePolling =
        flow {
            var latest = DEVICE_VOLUME_MIN
            while (true) {
                latest =
                    try {
                        getDeviceVolume()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (
                        @Suppress("TooGenericExceptionCaught") e: Exception,
                    ) {
                        latest
                    }
                emit(latest)
                delay(DEVICE_VOLUME_POLLING_INTERVAL_MS)
            }
        }

    val uiState: StateFlow<OtherListUiState> =
        combine(
            _uiState,
            settingsUseCases.observeOverlayVisible(),
            settingsUseCases.observeKeepScreenOn(),
            settingsUseCases.observeDynamicColorEnabled(),
            settingsUseCases.observeHapticFeedbackEnabled(),
        ) { state, overlayVisible, keepScreenOn, dynamicColorEnabled, hapticFeedbackEnabled ->
            state.copy(
                overlayVisible = overlayVisible,
                keepScreenOn = keepScreenOn,
                dynamicColorEnabled = dynamicColorEnabled,
                hapticFeedbackEnabled = hapticFeedbackEnabled,
            )
        }.combine(settingsUseCases.observeServerIp?.invoke() ?: flowOf(null)) { state, serverIp ->
            state.copy(serverIp = serverIp)
        }.combine(settingsUseCases.observeConsoleAddress()) { state, consoleAddress ->
            state.copy(consoleAddress = consoleAddress)
        }.combine(settingsUseCases.observeGt7Ps5UdpPort()) { state, consolePort ->
            state.copy(consolePort = consolePort)
        }.combine(settingsUseCases.observeThemeMode()) { state, themeMode ->
            state.copy(themeMode = themeMode)
        }.combine(settingsUseCases.observeVoice()) { state, voiceId ->
            state.copy(voiceId = voiceId)
        }.combine(settingsUseCases.observeVoiceSpeed()) { state, voiceSpeed ->
            state.copy(voiceSpeed = voiceSpeed)
        }.combine(settingsUseCases.observeVoicePitch()) { state, voicePitch ->
            state.copy(voicePitch = voicePitch)
        }.combine(settingsUseCases.observeReadoutStartSoundType()) { state, type ->
            state.copy(readoutStartSoundType = type)
        }.combine(settingsUseCases.observeSoundVolume()) { state, soundVolume ->
            state.copy(soundVolume = soundVolume)
        }.combine(deviceVolumePolling) { state, deviceVolume ->
            state.copy(deviceVolume = deviceVolume)
        }.combine(textToSpeechUnavailableReason) { state, ttsUnavailableReason ->
            state.copy(
                items = state.items.withTtsGuidance(ttsUnavailableReason),
                ttsUnavailableGuidance = ttsUnavailableReason?.toGuidance(),
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), _uiState.value)

    fun checkUpdate() {
        if (currentVersion.isBlank()) return
        viewModelScope.launch {
            val hasUpdate = checkAppUpdateAvailable(currentVersion)
            _uiState.update { it.copy(hasAppUpdate = hasUpdate) }
        }
    }

    fun checkAccessLocalNetworkPermission() {
        val granted = checkAccessLocalNetworkPermissionGranted()
        _uiState.update { it.copy(accessLocalNetworkPermissionGranted = granted) }
    }

    fun openWindowsSpeechSettings() {
        openWindowsSpeechSettings.invoke()
    }

    fun checkStartupEnabled() {
        viewModelScope.launch {
            val enabled = startupRegistration.getEnabled()
            _uiState.update { it.copy(startupEnabled = enabled) }
        }
    }

    fun checkTextToSpeechAvailability() {
        viewModelScope.launch {
            val reason = checkTextToSpeechUnavailableReason()
            textToSpeechUnavailableReason.update { reason }
        }
    }

    fun onStartupEnabledChange(enabled: Boolean) {
        viewModelScope.launch {
            startupRegistration.setEnabled(enabled)
            _uiState.update { it.copy(startupEnabled = enabled) }
        }
    }

    fun onItemSelected(itemType: OtherListItemType) {
        if (
            itemType == OtherListItemType.VoicePitch ||
            itemType == OtherListItemType.GitHubRepository ||
            itemType == OtherListItemType.ReleasePage ||
            itemType == OtherListItemType.AccessLocalNetworkPermission
        ) {
            return
        }
        _uiState.update { current ->
            current.copy(
                selectedItem = if (current.selectedItem == itemType) null else itemType,
                selectedFeedbackTelemetryLogId = null,
            )
        }
    }

    fun selectItem(itemType: OtherListItemType) {
        _uiState.update { it.copy(selectedItem = itemType, selectedFeedbackTelemetryLogId = null) }
    }

    fun selectFeedbackItem(telemetryLogId: Long) {
        _uiState.update {
            it.copy(
                selectedItem = OtherListItemType.Feedback,
                selectedFeedbackTelemetryLogId = telemetryLogId,
                feedbackAttachRequestId = it.feedbackAttachRequestId + 1,
            )
        }
    }

    fun clearSelectedItem() {
        _uiState.update { it.copy(selectedItem = null, selectedFeedbackTelemetryLogId = null) }
    }

    fun onOverlayVisibleChange(visible: Boolean) {
        viewModelScope.launch { settingsUseCases.saveOverlayVisible(visible) }
    }

    fun onKeepScreenOnChange(enabled: Boolean) {
        viewModelScope.launch { settingsUseCases.saveKeepScreenOn(enabled) }
    }

    fun onDynamicColorEnabledChange(enabled: Boolean) {
        viewModelScope.launch { settingsUseCases.saveDynamicColorEnabled(enabled) }
    }

    fun onHapticFeedbackEnabledChange(enabled: Boolean) {
        viewModelScope.launch { settingsUseCases.saveHapticFeedbackEnabled(enabled) }
    }

    private companion object {
        const val DEVICE_VOLUME_POLLING_INTERVAL_MS = 2_000L
    }
}

/**
 * TTS案内項目を [reason] に対応する1つだけに差し替える。
 * 追加した項目がセクション内の並びを崩さないよう、enumの宣言順に並べ直す。
 */
private fun List<OtherListItemType>.withTtsGuidance(reason: TextToSpeechUnavailableReason?): List<OtherListItemType> {
    val guidance =
        when (reason) {
            TextToSpeechUnavailableReason.EngineMissing -> OtherListItemType.TtsEngineMissing
            TextToSpeechUnavailableReason.LanguageDataMissing -> OtherListItemType.TtsLanguageDataMissing
            TextToSpeechUnavailableReason.WindowsSpeechUnavailable -> OtherListItemType.WindowsSpeechUnavailable
            null -> null
        }
    return (
        filterNot {
            it == OtherListItemType.WindowsSpeechUnavailable ||
                it == OtherListItemType.TtsEngineMissing ||
                it == OtherListItemType.TtsLanguageDataMissing
        } + listOfNotNull(guidance)
    ).sortedBy { it.ordinal }
}

private fun TextToSpeechUnavailableReason.toGuidance(): TtsUnavailableGuidance =
    when (this) {
        TextToSpeechUnavailableReason.EngineMissing -> TtsUnavailableGuidance.EngineMissing
        TextToSpeechUnavailableReason.LanguageDataMissing -> TtsUnavailableGuidance.LanguageDataMissing
        TextToSpeechUnavailableReason.WindowsSpeechUnavailable -> TtsUnavailableGuidance.WindowsSpeechUnavailable
    }
