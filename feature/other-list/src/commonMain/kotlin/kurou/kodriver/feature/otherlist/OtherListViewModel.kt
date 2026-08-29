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
import kurou.kodriver.domain.usecase.CheckAppUpdateAvailableUseCase
import kurou.kodriver.domain.usecase.CheckHapticFeedbackAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveDynamicColorEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveHapticFeedbackEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveKeepScreenOnEnabledUseCase
import kurou.kodriver.domain.usecase.SaveDynamicColorEnabledUseCase
import kurou.kodriver.domain.usecase.SaveHapticFeedbackEnabledUseCase
import kurou.kodriver.domain.usecase.SaveKeepScreenOnEnabledUseCase
import kurou.kodriver.domain.usecase.StartupRegistrationUseCases

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
    private val checkAppUpdateAvailable: CheckAppUpdateAvailableUseCase,
    observeKeepScreenOn: ObserveKeepScreenOnEnabledUseCase,
    private val saveKeepScreenOn: SaveKeepScreenOnEnabledUseCase,
    observeDynamicColorEnabled: ObserveDynamicColorEnabledUseCase,
    private val saveDynamicColorEnabled: SaveDynamicColorEnabledUseCase,
    observeHapticFeedbackEnabled: ObserveHapticFeedbackEnabledUseCase,
    private val saveHapticFeedbackEnabled: SaveHapticFeedbackEnabledUseCase,
    checkHapticFeedbackAvailable: CheckHapticFeedbackAvailableUseCase,
    private val startupRegistration: StartupRegistrationUseCases,
    appVersionInfo: OtherListAppVersionInfo,
) : ViewModel() {
    private val currentVersion = appVersionInfo.currentVersion
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
            ),
        )
    val uiState: StateFlow<OtherListUiState> =
        combine(
            _uiState,
            observeKeepScreenOn(),
            observeDynamicColorEnabled(),
            observeHapticFeedbackEnabled(),
        ) { state, keepScreenOn, dynamicColorEnabled, hapticFeedbackEnabled ->
            state.copy(
                keepScreenOn = keepScreenOn,
                dynamicColorEnabled = dynamicColorEnabled,
                hapticFeedbackEnabled = hapticFeedbackEnabled,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), _uiState.value)

    fun checkUpdate() {
        if (currentVersion.isBlank()) return
        viewModelScope.launch {
            val hasUpdate = checkAppUpdateAvailable(currentVersion)
            _uiState.update { it.copy(hasAppUpdate = hasUpdate) }
        }
    }

    fun checkStartupEnabled() {
        viewModelScope.launch {
            val enabled = startupRegistration.getEnabled()
            _uiState.update { it.copy(startupEnabled = enabled) }
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

    fun onKeepScreenOnChange(enabled: Boolean) {
        viewModelScope.launch { saveKeepScreenOn(enabled) }
    }

    fun onDynamicColorEnabledChange(enabled: Boolean) {
        viewModelScope.launch { saveDynamicColorEnabled(enabled) }
    }

    fun onHapticFeedbackEnabledChange(enabled: Boolean) {
        viewModelScope.launch { saveHapticFeedbackEnabled(enabled) }
    }
}
