package kurou.kodriver.feature.otherlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.usecase.CheckAppUpdateAvailableUseCase

class OtherListViewModel(
    private val checkAppUpdateAvailable: CheckAppUpdateAvailableUseCase,
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

    fun clearSelectedItem() {
        _uiState.update { it.copy(selectedItem = null) }
    }
}
