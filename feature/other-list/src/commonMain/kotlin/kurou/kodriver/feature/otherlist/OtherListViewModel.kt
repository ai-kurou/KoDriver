package kurou.kodriver.feature.otherlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.gt7ps5.usecase.ObserveGt7Ps5UdpPortUseCase
import kurou.kodriver.domain.usecase.CheckAccessLocalNetworkPermissionGrantedUseCase
import kurou.kodriver.domain.usecase.CheckHapticFeedbackAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveConsoleAddressUseCase
import kurou.kodriver.domain.usecase.ObserveOverlayBackgroundOpacityUseCase
import kurou.kodriver.domain.usecase.ObserveOverlayTextColorUseCase
import kurou.kodriver.domain.usecase.ObserveOverlayTextSizeUseCase
import kurou.kodriver.domain.usecase.ObserveOverlayVisibleUseCase
import kurou.kodriver.domain.usecase.ObserveServerIpUseCase
import kurou.kodriver.domain.usecase.OpenWindowsSpeechSettingsUseCase
import kurou.kodriver.domain.usecase.SaveOverlayVisibleUseCase

/**
 * アプリバージョン表示に必要な情報（現在バージョン・プラットフォーム別ラベル）。
 */
data class OtherListAppVersionInfo(
    val currentVersion: String,
    val appVersionLabel: String,
)

data class OtherListOverlayUseCases(
    val observeOverlayVisible: ObserveOverlayVisibleUseCase,
    val saveOverlayVisible: SaveOverlayVisibleUseCase,
    val observeOverlayTextSize: ObserveOverlayTextSizeUseCase,
    val observeOverlayBackgroundOpacity: ObserveOverlayBackgroundOpacityUseCase,
    val observeOverlayTextColor: ObserveOverlayTextColorUseCase,
)

data class OtherListConnectionUseCases(
    val observeServerIp: ObserveServerIpUseCase?,
    val observeConsoleAddress: ObserveConsoleAddressUseCase,
    val observeGt7Ps5UdpPort: ObserveGt7Ps5UdpPortUseCase,
)

/**
 * OtherList 画面の状態管理とユーザー操作を扱う ViewModel。
 */
@Suppress("LongParameterList")
class OtherListViewModel(
    private val overlayUseCases: OtherListOverlayUseCases,
    private val speechSettingsFlow: OtherListSpeechSettingsFlow,
    private val deviceSettingsFlow: OtherListDeviceSettingsFlow,
    private val connectionUseCases: OtherListConnectionUseCases,
    checkHapticFeedbackAvailable: CheckHapticFeedbackAvailableUseCase,
    private val checkAccessLocalNetworkPermissionGranted: CheckAccessLocalNetworkPermissionGrantedUseCase,
    private val openWindowsSpeechSettings: OpenWindowsSpeechSettingsUseCase,
    private val startupRegistrationFlow: OtherListStartupRegistrationFlow,
    private val ttsAvailabilityFlow: OtherListTtsAvailabilityFlow,
    appVersionInfo: OtherListAppVersionInfo,
    private val appUpdateFlow: OtherListAppUpdateFlow,
    private val deviceVolumeFlow: OtherListDeviceVolumeFlow,
    private val resetFlow: OtherListResetFlow,
) : ViewModel() {
    private val hapticFeedbackAvailable = checkHapticFeedbackAvailable()
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

    val uiState: StateFlow<OtherListUiState> =
        combine(
            _uiState,
            overlayUseCases.observeOverlayVisible(),
            deviceSettingsFlow.uiState,
            speechSettingsFlow.uiState,
        ) { state, overlayVisible, deviceSettings, speechSettings ->
            state.copy(
                overlayVisible = overlayVisible,
                keepScreenOn = deviceSettings.keepScreenOn,
                dynamicColorEnabled = deviceSettings.dynamicColorEnabled,
                hapticFeedbackEnabled = deviceSettings.hapticFeedbackEnabled,
                themeMode = deviceSettings.themeMode,
                voiceId = speechSettings.voiceId,
                voiceSpeed = speechSettings.voiceSpeed,
                voicePitch = speechSettings.voicePitch,
                androidAudioUsage = speechSettings.androidAudioUsage,
                readoutStartSoundType = speechSettings.readoutStartSoundType,
                soundVolume = speechSettings.soundVolume,
            )
        }.combine(connectionUseCases.observeServerIp?.invoke() ?: flowOf(null)) { state, serverIp ->
            state.copy(serverIp = serverIp)
        }.combine(connectionUseCases.observeConsoleAddress()) { state, consoleAddress ->
            state.copy(consoleAddress = consoleAddress)
        }.combine(connectionUseCases.observeGt7Ps5UdpPort()) { state, consolePort ->
            state.copy(consolePort = consolePort)
        }.combine(overlayUseCases.observeOverlayTextSize()) { state, overlayTextSize ->
            state.copy(overlayTextSize = overlayTextSize)
        }.combine(overlayUseCases.observeOverlayTextColor()) { state, overlayTextColor ->
            state.copy(overlayTextColor = overlayTextColor)
        }.combine(overlayUseCases.observeOverlayBackgroundOpacity()) { state, overlayBackgroundOpacity ->
            state.copy(overlayBackgroundOpacity = overlayBackgroundOpacity)
        }.combine(deviceVolumeFlow.volume) { state, deviceVolume ->
            state.copy(deviceVolume = deviceVolume)
        }.combine(ttsAvailabilityFlow.uiState) { state, ttsAvailability ->
            state.copy(
                items = ttsAvailabilityFlow.applyGuidance(state.items),
                ttsUnavailableGuidance = ttsAvailability.ttsUnavailableGuidance,
            )
        }.combine(resetFlow.uiState) { state, reset ->
            state.copy(reset = reset)
        }.combine(startupRegistrationFlow.uiState) { state, startupRegistration ->
            state.copy(startupEnabled = startupRegistration.startupEnabled)
        }.combine(appUpdateFlow.uiState) { state, appUpdate ->
            state.copy(hasAppUpdate = appUpdate.hasAppUpdate)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), _uiState.value)

    fun onResetClick() {
        resetFlow.onResetClick()
    }

    fun onResetDismiss() {
        resetFlow.onResetDismiss()
    }

    fun onResetConfirm() {
        viewModelScope.launch { resetFlow.onResetConfirm() }
    }

    fun consumeResetResult() {
        resetFlow.consumeResetResult()
    }

    fun checkUpdate() {
        viewModelScope.launch { appUpdateFlow.checkUpdate() }
    }

    fun checkAccessLocalNetworkPermission() {
        val granted = checkAccessLocalNetworkPermissionGranted()
        _uiState.update { it.copy(accessLocalNetworkPermissionGranted = granted) }
    }

    fun openWindowsSpeechSettings() {
        openWindowsSpeechSettings.invoke()
    }

    fun checkStartupEnabled() {
        viewModelScope.launch { startupRegistrationFlow.checkStartupEnabled() }
    }

    fun checkTextToSpeechAvailability() {
        viewModelScope.launch { ttsAvailabilityFlow.checkAvailability() }
    }

    fun onStartupEnabledChange(enabled: Boolean) {
        viewModelScope.launch { startupRegistrationFlow.onStartupEnabledChange(enabled) }
    }

    fun onItemSelected(itemType: OtherListItemType) {
        if (
            itemType == OtherListItemType.DeleteAllLogs ||
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
        if (itemType == OtherListItemType.DeleteAllLogs) {
            return
        }
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
        viewModelScope.launch { overlayUseCases.saveOverlayVisible(visible) }
    }

    fun onKeepScreenOnChange(enabled: Boolean) {
        viewModelScope.launch { deviceSettingsFlow.onKeepScreenOnChange(enabled) }
    }

    fun onDynamicColorEnabledChange(enabled: Boolean) {
        viewModelScope.launch { deviceSettingsFlow.onDynamicColorEnabledChange(enabled) }
    }

    fun onHapticFeedbackEnabledChange(enabled: Boolean) {
        viewModelScope.launch { deviceSettingsFlow.onHapticFeedbackEnabledChange(enabled) }
    }
}
