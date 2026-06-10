package kurou.kodriver.feature.other

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class OtherViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(OtherListUiState())
    val uiState: StateFlow<OtherListUiState> = _uiState.asStateFlow()

    fun onItemSelected(itemId: String) {
        val itemType = OtherItemType.fromId(itemId) ?: return
        if (itemType == OtherItemType.GitHubRepository) return
        _uiState.update { current ->
            current.copy(selectedItem = if (current.selectedItem == itemType) null else itemType)
        }
    }

    fun clearSelectedItem() {
        _uiState.update { it.copy(selectedItem = null) }
    }
}
