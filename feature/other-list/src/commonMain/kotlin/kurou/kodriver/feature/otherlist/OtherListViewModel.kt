package kurou.kodriver.feature.otherlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
@Suppress("LongParameterList", "TooManyFunctions")
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
    private val autoReadoutSettingsResetFlow: OtherListAutoReadoutSettingsResetFlow,
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
        OtherListFlow(
            baseState = _uiState,
            overlaySettings = overlaySettingsFlow.uiState,
            deviceSettings = deviceSettingsFlow.uiState,
            speechSettings = speechSettingsFlow.uiState,
            connection = connectionFlow.uiState,
            deviceVolume = deviceVolumeFlow.volume,
            ttsAvailability = ttsAvailabilityFlow.uiState,
            applyGuidance = ttsAvailabilityFlow::applyGuidance,
            reset = resetFlow.uiState,
            autoReadoutSettingsReset = autoReadoutSettingsResetFlow.uiState,
            startupRegistration = startupRegistrationFlow.uiState,
            appUpdate = appUpdateFlow.uiState,
        ).uiState.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), _uiState.value)

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

    fun onAutoReadoutSettingsResetClick() {
        autoReadoutSettingsResetFlow.onResetClick()
    }

    fun onAutoReadoutSettingsResetDismiss() {
        autoReadoutSettingsResetFlow.onResetDismiss()
    }

    fun onAutoReadoutSettingsResetConfirm() {
        viewModelScope.launch { autoReadoutSettingsResetFlow.onResetConfirm() }
    }

    fun consumeAutoReadoutSettingsResetResult() {
        autoReadoutSettingsResetFlow.consumeResetResult()
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
            itemType == OtherListItemType.ResetAutoReadoutSettings ||
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
        if (
            itemType == OtherListItemType.DeleteAllLogs ||
            itemType == OtherListItemType.ResetAutoReadoutSettings
        ) {
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
