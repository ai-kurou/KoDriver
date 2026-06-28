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
import kurou.kodriver.domain.usecase.ObserveExitConfirmationEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveKeepScreenOnUseCase
import kurou.kodriver.domain.usecase.SaveExitConfirmationEnabledUseCase

class AppScreenViewModel(
    private val checkAppUpdateAvailable: CheckAppUpdateAvailableUseCase,
    private val currentVersion: String,
    observeKeepScreenOn: ObserveKeepScreenOnUseCase,
    observeExitConfirmationEnabled: ObserveExitConfirmationEnabledUseCase,
    private val saveExitConfirmationEnabled: SaveExitConfirmationEnabledUseCase,
) : ViewModel() {

    private val _hasAppUpdate = MutableStateFlow(false)

    val uiState: StateFlow<AppScreenUiState> = combine(
        _hasAppUpdate,
        observeKeepScreenOn(),
        observeExitConfirmationEnabled(),
    ) { hasUpdate, keepOn, exitConfirmation ->
        AppScreenUiState(hasAppUpdate = hasUpdate, keepScreenOn = keepOn, exitConfirmationEnabled = exitConfirmation)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppScreenUiState())

    fun checkUpdate() {
        if (currentVersion.isBlank()) return
        viewModelScope.launch {
            val hasUpdate = checkAppUpdateAvailable(currentVersion)
            _hasAppUpdate.update { hasUpdate }
        }
    }

    suspend fun saveExitConfirmationEnabled(enabled: Boolean) {
        saveExitConfirmationEnabled.invoke(enabled)
    }
}

data class AppScreenUiState(
    val hasAppUpdate: Boolean = false,
    val keepScreenOn: Boolean = true,
    val exitConfirmationEnabled: Boolean = true,
)
