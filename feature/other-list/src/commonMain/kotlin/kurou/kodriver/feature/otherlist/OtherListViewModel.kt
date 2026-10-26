package kurou.kodriver.feature.otherlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.usecase.CheckAccessLocalNetworkPermissionGrantedUseCase
import kurou.kodriver.domain.usecase.CheckHapticFeedbackAvailableUseCase
import kurou.kodriver.domain.usecase.OpenWindowsSpeechSettingsUseCase

/**
 * アプリバージョン表示に必要な情報（現在バージョン・プラットフォーム別ラベル）。
 */
data class OtherListAppVersionInfo(
    val currentVersion: String,
    val appVersionLabel: String,
)

/**
 * OtherList 画面の状態管理とユーザー操作を扱う ViewModel。
 */
@Suppress("LongParameterList")
class OtherListViewModel(
    private val overlaySettingsFlow: OtherListOverlaySettingsFlow,
    private val speechSettingsFlow: OtherListSpeechSettingsFlow,
    private val deviceSettingsFlow: OtherListDeviceSettingsFlow,
    private val connectionFlow: OtherListConnectionFlow,
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
            overlaySettingsFlow.uiState,
            deviceSettingsFlow.uiState,
            speechSettingsFlow.uiState,
            connectionFlow.uiState,
        ) { state, overlaySettings, deviceSettings, speechSettings, connection ->
            state.copy(
                overlayVisible = overlaySettings.overlayVisible,
                overlayTextSize = overlaySettings.overlayTextSize,
                overlayTextColor = overlaySettings.overlayTextColor,
                overlayBackgroundOpacity = overlaySettings.overlayBackgroundOpacity,
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
                serverIp = connection.serverIp,
                consoleAddress = connection.consoleAddress,
                consolePort = connection.consolePort,
            )
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
        viewModelScope.launch { overlaySettingsFlow.onOverlayVisibleChange(visible) }
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
