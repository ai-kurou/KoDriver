package kurou.kodriver.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.usecase.CheckAppUpdateAvailableUseCase

class AppScreenViewModel(
    private val checkAppUpdateAvailable: CheckAppUpdateAvailableUseCase,
    private val currentVersion: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppScreenUiState())
    val uiState: StateFlow<AppScreenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val hasUpdate = checkAppUpdateAvailable(currentVersion)
            _uiState.update { it.copy(hasAppUpdate = hasUpdate) }
        }
    }
}

data class AppScreenUiState(
    val hasAppUpdate: Boolean = false,
)
