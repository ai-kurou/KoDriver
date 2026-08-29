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
import kurou.kodriver.domain.usecase.CheckAccessLocalNetworkPermissionGrantedUseCase
import kurou.kodriver.domain.usecase.CheckAppUpdateAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveDynamicColorEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveEffectiveKeepScreenOnUseCase
import kurou.kodriver.domain.usecase.ObserveHapticFeedbackEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.SaveSelectedSimulatorUseCase

/**
 * AppScreen 画面の状態管理とユーザー操作を扱う ViewModel。
 */
@Suppress("LongParameterList")
class AppScreenViewModel(
    private val checkAppUpdateAvailable: CheckAppUpdateAvailableUseCase,
    private val currentVersion: String,
    observeEffectiveKeepScreenOn: ObserveEffectiveKeepScreenOnUseCase,
    observeDynamicColorEnabled: ObserveDynamicColorEnabledUseCase,
    observeHapticFeedbackEnabled: ObserveHapticFeedbackEnabledUseCase,
    observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
    private val saveSelectedSimulator: SaveSelectedSimulatorUseCase,
    private val checkAccessLocalNetworkPermissionGranted: CheckAccessLocalNetworkPermissionGrantedUseCase,
) : ViewModel() {
    private val _hasAppUpdate = MutableStateFlow(false)
    private val _accessLocalNetworkPermissionGranted = MutableStateFlow(checkAccessLocalNetworkPermissionGranted())
    private val badgeState =
        combine(_hasAppUpdate, _accessLocalNetworkPermissionGranted) { hasUpdate, accessLocalNetworkPermissionGranted ->
            hasUpdate to accessLocalNetworkPermissionGranted
        }

    val uiState: StateFlow<AppScreenUiState> =
        combine(
            badgeState,
            observeEffectiveKeepScreenOn(),
            observeDynamicColorEnabled(),
            observeHapticFeedbackEnabled(),
            observeSelectedSimulator(),
        ) { (hasUpdate, permissionGranted), keepOn, dynamicColorEnabled, hapticFeedbackEnabled, selectedSimulator ->
            AppScreenUiState(
                hasAppUpdate = hasUpdate,
                accessLocalNetworkPermissionGranted = permissionGranted,
                keepScreenOn = keepOn,
                dynamicColorEnabled = dynamicColorEnabled,
                hapticFeedbackEnabled = hapticFeedbackEnabled,
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

    fun checkAccessLocalNetworkPermission() {
        _accessLocalNetworkPermissionGranted.update { checkAccessLocalNetworkPermissionGranted() }
    }

    /**
     * app:shared など `:core:domain` に依存しないモジュールから呼び出せるよう、
     * [Simulator] 型ではなく [Simulator.id] の文字列を受け取る。不明な ID は無視する。
     */
    fun selectSimulator(simulatorId: String) {
        val simulator = Simulator.fromId(simulatorId) ?: return
        viewModelScope.launch {
            saveSelectedSimulator(simulator)
        }
    }
}
