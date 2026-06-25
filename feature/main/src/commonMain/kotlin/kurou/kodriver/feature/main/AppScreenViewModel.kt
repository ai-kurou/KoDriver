package kurou.kodriver.feature.main

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
import kurou.kodriver.domain.usecase.ObserveKeepScreenOnUseCase

class AppScreenViewModel(
    private val checkAppUpdateAvailable: CheckAppUpdateAvailableUseCase,
    private val currentVersion: String,
    observeKeepScreenOn: ObserveKeepScreenOnUseCase,
) : ViewModel() {

    private val _hasAppUpdate = MutableStateFlow(false)

    val uiState: StateFlow<AppScreenUiState> = combine(_hasAppUpdate, observeKeepScreenOn()) { hasUpdate, keepOn ->
        AppScreenUiState(hasAppUpdate = hasUpdate, keepScreenOn = keepOn)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppScreenUiState())

    fun checkUpdate() {
        if (currentVersion.isBlank()) return
        viewModelScope.launch {
            val hasUpdate = checkAppUpdateAvailable(currentVersion)
            _hasAppUpdate.update { hasUpdate }
        }
    }
}

data class AppScreenUiState(
    val hasAppUpdate: Boolean = false,
    val keepScreenOn: Boolean = true,
)
