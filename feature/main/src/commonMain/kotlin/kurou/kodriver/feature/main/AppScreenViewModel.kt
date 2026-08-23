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
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.CheckAppUpdateAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveDynamicColorEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveKeepScreenOnEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.SaveSelectedSimulatorUseCase

/**
 * AppScreen 画面の状態管理とユーザー操作を扱う ViewModel。
 */
class AppScreenViewModel(
    private val checkAppUpdateAvailable: CheckAppUpdateAvailableUseCase,
    private val currentVersion: String,
    observeKeepScreenOn: ObserveKeepScreenOnEnabledUseCase,
    observeDynamicColorEnabled: ObserveDynamicColorEnabledUseCase,
    observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
    private val saveSelectedSimulator: SaveSelectedSimulatorUseCase,
) : ViewModel() {
    private val _hasAppUpdate = MutableStateFlow(false)

    val uiState: StateFlow<AppScreenUiState> =
        combine(
            _hasAppUpdate,
            observeKeepScreenOn(),
            observeDynamicColorEnabled(),
            observeSelectedSimulator(),
        ) { hasUpdate, keepOn, dynamicColorEnabled, selectedSimulator ->
            AppScreenUiState(
                hasAppUpdate = hasUpdate,
                keepScreenOn = keepOn,
                dynamicColorEnabled = dynamicColorEnabled,
                selectedSimulator = selectedSimulator,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppScreenUiState())

    fun checkUpdate() {
        if (currentVersion.isBlank()) return
        viewModelScope.launch {
            val hasUpdate = checkAppUpdateAvailable(currentVersion)
            _hasAppUpdate.update { hasUpdate }
        }
    }

    fun selectSimulator(simulator: Simulator) {
        viewModelScope.launch {
            saveSelectedSimulator(simulator)
        }
    }
}
