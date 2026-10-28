package kurou.kodriver.feature.acewindowsconnection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase

internal class AceWindowsConnectionViewModel(
    private val observeAceWindowsConnection: ObserveAceWindowsConnectionUseCase,
    private val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
) : ViewModel() {
    val uiState: StateFlow<AceWindowsConnectionUiState> =
        AceWindowsConnectionFlow(
            selectedSimulator = observeSelectedSimulator(),
            observeAceWindowsConnection = { observeAceWindowsConnection() },
        ).uiState
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = AceWindowsConnectionUiState(),
            )
}
