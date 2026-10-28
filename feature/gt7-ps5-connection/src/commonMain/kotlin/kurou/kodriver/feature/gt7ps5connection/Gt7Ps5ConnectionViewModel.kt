package kurou.kodriver.feature.gt7ps5connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.gt7ps5.usecase.ObserveGt7Ps5ConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase

internal class Gt7Ps5ConnectionViewModel(
    private val observeGt7Ps5Connection: ObserveGt7Ps5ConnectionUseCase,
    private val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
) : ViewModel() {
    val uiState: StateFlow<Gt7Ps5ConnectionUiState> =
        Gt7Ps5ConnectionFlow(
            selectedSimulator = observeSelectedSimulator(),
            observeGt7Ps5Connection = { observeGt7Ps5Connection() },
        ).uiState
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = Gt7Ps5ConnectionUiState(),
            )
}
