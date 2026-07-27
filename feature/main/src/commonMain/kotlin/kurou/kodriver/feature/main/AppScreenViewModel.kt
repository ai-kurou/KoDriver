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
import kurou.kodriver.domain.model.DYNAMIC_COLOR_ENABLED_DEFAULT
import kurou.kodriver.domain.model.EXIT_CONFIRMATION_ENABLED_DEFAULT
import kurou.kodriver.domain.model.KEEP_SCREEN_ON_ENABLED_DEFAULT
import kurou.kodriver.domain.usecase.CheckAppUpdateAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveDynamicColorEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveExitConfirmationEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveKeepScreenOnEnabledUseCase
import kurou.kodriver.domain.usecase.SaveExitConfirmationEnabledUseCase

class AppScreenViewModel(
    private val checkAppUpdateAvailable: CheckAppUpdateAvailableUseCase,
    private val currentVersion: String,
    observeKeepScreenOn: ObserveKeepScreenOnEnabledUseCase,
    observeExitConfirmationEnabled: ObserveExitConfirmationEnabledUseCase,
    private val saveExitConfirmationEnabled: SaveExitConfirmationEnabledUseCase,
    observeDynamicColorEnabled: ObserveDynamicColorEnabledUseCase,
) : ViewModel() {

    private val _hasAppUpdate = MutableStateFlow(false)

    val uiState: StateFlow<AppScreenUiState> = combine(
        _hasAppUpdate,
        observeKeepScreenOn(),
        observeExitConfirmationEnabled(),
        observeDynamicColorEnabled(),
    ) { hasUpdate, keepOn, exitConfirmation, dynamicColorEnabled ->
        AppScreenUiState(
            hasAppUpdate = hasUpdate,
            keepScreenOn = keepOn,
            exitConfirmationEnabled = exitConfirmation,
            dynamicColorEnabled = dynamicColorEnabled,
        )
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
    val keepScreenOn: Boolean = KEEP_SCREEN_ON_ENABLED_DEFAULT,
    val exitConfirmationEnabled: Boolean = EXIT_CONFIRMATION_ENABLED_DEFAULT,
    val dynamicColorEnabled: Boolean = DYNAMIC_COLOR_ENABLED_DEFAULT,
)
