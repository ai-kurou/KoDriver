package kurou.kodriver.feature.otherlist

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class OtherListViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(OtherListUiState())
    val uiState: StateFlow<OtherListUiState> = _uiState.asStateFlow()

    fun onItemSelected(itemId: String) {
        val itemType = OtherListItemType.fromId(itemId) ?: return
        if (
            itemType == OtherListItemType.Volume ||
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
