package kurou.kodriver.feature.debugstatedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase

internal class DebugStateDetailViewModel(
    observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
    observeRaceFlags: ObserveLmuWindowsRaceFlagsUseCase,
) : ViewModel() {

    val uiState: StateFlow<DebugStateDetailUiState> = combine(
        observeSelectedSimulator(),
        observeRaceFlags(),
    ) { selectedSimulator, raceFlags ->
        DebugStateDetailUiState(selectedSimulator = selectedSimulator, raceFlags = raceFlags)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DebugStateDetailUiState())
}
