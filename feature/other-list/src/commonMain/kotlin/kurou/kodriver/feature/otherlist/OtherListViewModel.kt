package kurou.kodriver.feature.otherlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.usecase.CheckAppUpdateAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveExitConfirmationEnabledUseCase
import kurou.kodriver.domain.usecase.SaveExitConfirmationEnabledUseCase

class OtherListViewModel(
    private val checkAppUpdateAvailable: CheckAppUpdateAvailableUseCase,
    observeExitConfirmationEnabled: ObserveExitConfirmationEnabledUseCase,
    private val saveExitConfirmationEnabled: SaveExitConfirmationEnabledUseCase,
    private val currentVersion: String,
    appVersionLabel: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        OtherListUiState(
            appVersionLabel = appVersionLabel,
            appVersion = currentVersion,
        ),
    )
    val uiState: StateFlow<OtherListUiState> = _uiState.asStateFlow()

    @Suppress("unused")
    private val exitConfirmationEnabledCollector = viewModelScope.launch {
        observeExitConfirmationEnabled().collect { exitConfirmationEnabled ->
            _uiState.update { it.copy(exitConfirmationEnabled = exitConfirmationEnabled) }
        }
    }

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
            current.copy(selectedItem = if (current.selectedItem == itemType) null else itemType)
        }
    }

    fun selectItem(itemType: OtherListItemType) {
        _uiState.update { it.copy(selectedItem = itemType) }
    }

    fun clearSelectedItem() {
        _uiState.update { it.copy(selectedItem = null) }
    }

    fun onExitConfirmationEnabledChange(enabled: Boolean) {
        _uiState.update { it.copy(exitConfirmationEnabled = enabled) }
        viewModelScope.launch { saveExitConfirmationEnabled(enabled) }
    }
}
