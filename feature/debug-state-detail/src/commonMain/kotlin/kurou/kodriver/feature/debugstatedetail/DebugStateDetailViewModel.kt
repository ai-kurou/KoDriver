package kurou.kodriver.feature.debugstatedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase

internal class DebugStateDetailViewModel(
    observeRaceFlags: ObserveLmuWindowsRaceFlagsUseCase,
) : ViewModel() {

    val uiState: StateFlow<DebugStateDetailUiState> = observeRaceFlags()
        .map { DebugStateDetailUiState(raceFlags = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DebugStateDetailUiState())
}
