package kurou.kodriver.feature.narratoroverlay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.usecase.ObserveLatestTelemetryLogUseCase

internal class NarratorOverlayViewModel(
    observeLatestTelemetryLog: ObserveLatestTelemetryLogUseCase,
) : ViewModel() {
    val uiState: StateFlow<NarratorOverlayUiState> =
        observeLatestTelemetryLog()
            .map { latestTelemetryLog -> NarratorOverlayUiState(latestTelemetryLog = latestTelemetryLog) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NarratorOverlayUiState())
}
