package kurou.kodriver.feature.narratoroverlay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.usecase.ObserveLatestNarratedTelemetryLogUseCase
import kurou.kodriver.domain.usecase.ObserveOverlayBackgroundOpacityUseCase
import kurou.kodriver.domain.usecase.ObserveOverlayTextColorUseCase
import kurou.kodriver.domain.usecase.ObserveOverlayTextSizeUseCase

internal class NarratorOverlayViewModel(
    observeLatestNarratedTelemetryLog: ObserveLatestNarratedTelemetryLogUseCase,
    observeOverlayTextSize: ObserveOverlayTextSizeUseCase,
    observeOverlayTextColor: ObserveOverlayTextColorUseCase,
    observeOverlayBackgroundOpacity: ObserveOverlayBackgroundOpacityUseCase,
) : ViewModel() {
    val uiState: StateFlow<NarratorOverlayUiState> =
        NarratorOverlayFlow(
            latestTelemetryLog = observeLatestNarratedTelemetryLog(),
            overlayTextSize = observeOverlayTextSize(),
            overlayTextColor = observeOverlayTextColor(),
            backgroundOpacity = observeOverlayBackgroundOpacity(),
        ).uiState.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NarratorOverlayUiState())
}
