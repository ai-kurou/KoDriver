package kurou.kodriver.feature.narratoroverlay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.usecase.ObserveLatestTelemetryLogUseCase
import kurou.kodriver.domain.usecase.ObserveOverlayTextSizeUseCase

internal class NarratorOverlayViewModel(
    observeLatestTelemetryLog: ObserveLatestTelemetryLogUseCase,
    observeOverlayTextSize: ObserveOverlayTextSizeUseCase,
) : ViewModel() {
    val uiState: StateFlow<NarratorOverlayUiState> =
        combine(observeLatestTelemetryLog(), observeOverlayTextSize()) { latestTelemetryLog, overlayTextSize ->
            NarratorOverlayUiState(latestTelemetryLog = latestTelemetryLog, overlayTextSize = overlayTextSize)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NarratorOverlayUiState())
}
