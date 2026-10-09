package kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBrakeWearRemainingUseCase

internal class LmuWindowsReadoutBrakeWearDetailViewModel(
    observeRemaining: ObserveLmuWindowsBrakeWearRemainingUseCase,
) : ViewModel() {
    val uiState: StateFlow<LmuWindowsReadoutBrakeWearDetailUiState> =
        observeRemaining()
            .map { LmuWindowsReadoutBrakeWearDetailUiState(remaining = it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                LmuWindowsReadoutBrakeWearDetailUiState(),
            )
}
