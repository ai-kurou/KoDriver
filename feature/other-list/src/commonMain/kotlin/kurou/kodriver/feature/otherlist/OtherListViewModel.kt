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
import kurou.kodriver.domain.usecase.ObserveDynamicColorEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveKeepScreenOnEnabledUseCase
import kurou.kodriver.domain.usecase.SaveDynamicColorEnabledUseCase
import kurou.kodriver.domain.usecase.SaveKeepScreenOnEnabledUseCase

/**
 * OtherList 画面の状態管理とユーザー操作を扱う ViewModel。
 */
class OtherListViewModel(
    private val checkAppUpdateAvailable: CheckAppUpdateAvailableUseCase,
    observeKeepScreenOn: ObserveKeepScreenOnEnabledUseCase,
    private val saveKeepScreenOn: SaveKeepScreenOnEnabledUseCase,
    observeDynamicColorEnabled: ObserveDynamicColorEnabledUseCase,
    private val saveDynamicColorEnabled: SaveDynamicColorEnabledUseCase,
    private val currentVersion: String,
    appVersionLabel: String,
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(
            OtherListUiState(
                appVersionLabel = appVersionLabel,
                appVersion = currentVersion,
            ),
        )
    val uiState: StateFlow<OtherListUiState> =
        combine(
            _uiState,
            observeKeepScreenOn(),
            observeDynamicColorEnabled(),
        ) { state, keepScreenOn, dynamicColorEnabled ->
            state.copy(
                keepScreenOn = keepScreenOn,
                dynamicColorEnabled = dynamicColorEnabled,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), _uiState.value)

    fun checkUpdate() {
        if (currentVersion.isBlank()) return
        viewModelScope.launch {
            val hasUpdate = checkAppUpdateAvailable(currentVersion)
            _uiState.update { it.copy(hasAppUpdate = hasUpdate) }
        }
    }

    fun onItemSelected(itemType: OtherListItemType) {
        if (
            itemType == OtherListItemType.GitHubRepository ||
            itemType == OtherListItemType.ReleasePage
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
            it.copy(selectedItem = OtherListItemType.Feedback, selectedFeedbackTelemetryLogId = telemetryLogId)
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
}
