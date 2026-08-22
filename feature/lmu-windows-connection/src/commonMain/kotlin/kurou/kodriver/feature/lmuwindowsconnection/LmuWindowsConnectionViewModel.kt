package kurou.kodriver.feature.lmuwindowsconnection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveLmuWindowsConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase

internal class LmuWindowsConnectionViewModel(
    private val observeLmuWindowsConnection: ObserveLmuWindowsConnectionUseCase,
    private val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
) : ViewModel() {
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<LmuWindowsConnectionUiState> =
        observeSelectedSimulator()
            .flatMapLatest { simulator ->
                if (simulator is Simulator.LmuWindows) {
                    observeLmuWindowsConnection().map { isConnected ->
                        LmuWindowsConnectionUiState(
                            connectionStatus =
                                if (isConnected) {
                                    LmuWindowsConnectionStatus.CONNECTED
                                } else {
                                    LmuWindowsConnectionStatus.DISCONNECTED
                                },
                        )
                    }
                } else {
                    flowOf(LmuWindowsConnectionUiState())
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = LmuWindowsConnectionUiState(),
            )
}
