package kurou.kodriver.feature.lmuwindowsconnection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase

internal class LmuWindowsConnectionViewModel(
    private val observeLmuWindowsConnection: ObserveLmuWindowsConnectionUseCase,
    private val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
) : ViewModel() {
    val uiState: StateFlow<LmuWindowsConnectionUiState> =
        LmuWindowsConnectionFlow(
            selectedSimulator = observeSelectedSimulator(),
            observeLmuWindowsConnection = { observeLmuWindowsConnection() },
        ).uiState
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = LmuWindowsConnectionUiState(),
            )
}
