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
import kurou.kodriver.domain.model.TextToSpeechUnavailableReason
import kurou.kodriver.domain.usecase.CheckAccessLocalNetworkPermissionGrantedUseCase
import kurou.kodriver.domain.usecase.CheckAppUpdateAvailableUseCase
import kurou.kodriver.domain.usecase.CheckHapticFeedbackAvailableUseCase
import kurou.kodriver.domain.usecase.CheckTextToSpeechUnavailableReasonUseCase
import kurou.kodriver.domain.usecase.ObserveDynamicColorEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveHapticFeedbackEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveKeepScreenOnEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveOverlayVisibleUseCase
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
    private val startupRegistration: StartupRegistrationUseCases,
    appVersionInfo: OtherListAppVersionInfo,
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
        }.combine(textToSpeechUnavailableReason) { state, ttsUnavailableReason ->
            state.copy(
                items =
                    state.items.filterNot {
                        it == OtherListItemType.WindowsSpeechUnavailable ||
                            it == OtherListItemType.TtsEngineMissing ||
                            it == OtherListItemType.TtsLanguageDataMissing
                    } +
                        when (ttsUnavailableReason) {
                            TextToSpeechUnavailableReason.EngineMissing -> listOf(OtherListItemType.TtsEngineMissing)
                            TextToSpeechUnavailableReason.LanguageDataMissing -> {
                                listOf(OtherListItemType.TtsLanguageDataMissing)
                            }

                            TextToSpeechUnavailableReason.WindowsSpeechUnavailable -> {
                                listOf(OtherListItemType.WindowsSpeechUnavailable)
                            }

                            null -> emptyList()
                        },
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

    fun checkStartupEnabled() {
        viewModelScope.launch {
            val enabled = startupRegistration.getEnabled()
            _uiState.update { it.copy(startupEnabled = enabled) }
        }
    }

    fun checkTextToSpeechAvailability() {
        viewModelScope.launch {
            textToSpeechUnavailableReason.value = checkTextToSpeechUnavailableReason()
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
}
