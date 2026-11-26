package kurou.kodriver.feature.otherlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.launchCapturingErrors
import kurou.kodriver.domain.serverconnection.usecase.CheckAccessLocalNetworkPermissionGrantedUseCase
import kurou.kodriver.domain.usecase.CheckHapticFeedbackAvailableUseCase
import kurou.kodriver.domain.usecase.OpenWindowsSpeechSettingsUseCase

/**
 * アプリバージョン表示に必要な情報（現在バージョン・プラットフォーム別ラベル）。
 */
public data class OtherListAppVersionInfo(
    val currentVersion: String,
    val appVersionLabel: String,
)

/**
 * OtherList 画面の状態管理とユーザー操作を扱う ViewModel。
 */
@Suppress("LongParameterList", "TooManyFunctions")
public class OtherListViewModel(
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
    private val autoReadoutSettingsResetFlow: OtherListResetFlow,
    private val manualReadoutSettingsResetFlow: OtherListResetFlow,
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

    public val uiState: StateFlow<OtherListUiState> =
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
            manualReadoutSettingsReset = manualReadoutSettingsResetFlow.uiState,
            startupRegistration = startupRegistrationFlow.uiState,
            appUpdate = appUpdateFlow.uiState,
        ).uiState.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), _uiState.value)

    public fun onResetClick() {
        resetFlow.onResetClick()
    }

    public fun onResetDismiss() {
        resetFlow.onResetDismiss()
    }

    public fun onResetConfirm() {
        viewModelScope.launch { resetFlow.onResetConfirm() }
    }

    public fun consumeResetResult() {
        resetFlow.consumeResetResult()
    }

    public fun onAutoReadoutSettingsResetClick() {
        autoReadoutSettingsResetFlow.onResetClick()
    }

    public fun onAutoReadoutSettingsResetDismiss() {
        autoReadoutSettingsResetFlow.onResetDismiss()
    }

    public fun onAutoReadoutSettingsResetConfirm() {
        viewModelScope.launch { autoReadoutSettingsResetFlow.onResetConfirm() }
    }

    public fun consumeAutoReadoutSettingsResetResult() {
        autoReadoutSettingsResetFlow.consumeResetResult()
    }

    public fun onManualReadoutSettingsResetClick() {
        manualReadoutSettingsResetFlow.onResetClick()
    }

    public fun onManualReadoutSettingsResetDismiss() {
        manualReadoutSettingsResetFlow.onResetDismiss()
    }

    public fun onManualReadoutSettingsResetConfirm() {
        viewModelScope.launch { manualReadoutSettingsResetFlow.onResetConfirm() }
    }

    public fun consumeManualReadoutSettingsResetResult() {
        manualReadoutSettingsResetFlow.consumeResetResult()
    }

    public fun checkUpdate() {
        viewModelScope.launchCapturingErrors { appUpdateFlow.checkUpdate() }
    }

    public fun checkAccessLocalNetworkPermission() {
        val granted = checkAccessLocalNetworkPermissionGranted()
        _uiState.update { it.copy(accessLocalNetworkPermissionGranted = granted) }
    }

    public fun openWindowsSpeechSettings() {
        openWindowsSpeechSettings.invoke()
    }

    public fun checkStartupEnabled() {
        viewModelScope.launchCapturingErrors { startupRegistrationFlow.checkStartupEnabled() }
    }

    public fun checkTextToSpeechAvailability() {
        viewModelScope.launchCapturingErrors { ttsAvailabilityFlow.checkAvailability() }
    }

    public fun onStartupEnabledChange(enabled: Boolean) {
        viewModelScope.launchCapturingErrors { startupRegistrationFlow.onStartupEnabledChange(enabled) }
    }

    public fun onItemSelected(itemType: OtherListItemType) {
        if (
            itemType == OtherListItemType.DeleteAllLogs ||
            itemType == OtherListItemType.ResetAutoReadoutSettings ||
            itemType == OtherListItemType.ResetManualReadoutSettings ||
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

    public fun selectItem(itemType: OtherListItemType) {
        if (
            itemType == OtherListItemType.DeleteAllLogs ||
            itemType == OtherListItemType.ResetAutoReadoutSettings ||
            itemType == OtherListItemType.ResetManualReadoutSettings
        ) {
            return
        }
        _uiState.update { it.copy(selectedItem = itemType, selectedFeedbackTelemetryLogId = null) }
    }

    public fun selectFeedbackItem(telemetryLogId: Long) {
        _uiState.update {
            it.copy(
                selectedItem = OtherListItemType.Feedback,
                selectedFeedbackTelemetryLogId = telemetryLogId,
                feedbackAttachRequestId = it.feedbackAttachRequestId + 1,
            )
        }
    }

    public fun clearSelectedItem() {
        _uiState.update { it.copy(selectedItem = null, selectedFeedbackTelemetryLogId = null) }
    }

    public fun onOverlayVisibleChange(visible: Boolean) {
        viewModelScope.launchCapturingErrors { overlaySettingsFlow.onOverlayVisibleChange(visible) }
    }

    public fun onKeepScreenOnChange(enabled: Boolean) {
        viewModelScope.launchCapturingErrors { deviceSettingsFlow.onKeepScreenOnChange(enabled) }
    }

    public fun onDynamicColorEnabledChange(enabled: Boolean) {
        viewModelScope.launchCapturingErrors { deviceSettingsFlow.onDynamicColorEnabledChange(enabled) }
    }

    public fun onHapticFeedbackEnabledChange(enabled: Boolean) {
        viewModelScope.launchCapturingErrors { deviceSettingsFlow.onHapticFeedbackEnabledChange(enabled) }
    }
}
