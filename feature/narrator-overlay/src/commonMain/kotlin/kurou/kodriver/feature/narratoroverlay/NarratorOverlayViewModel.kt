package kurou.kodriver.feature.narratoroverlay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.usecase.ObserveLatestNarratedTelemetryLogUseCase
import kurou.kodriver.domain.usecase.ObserveOverlayBackgroundOpacityUseCase
import kurou.kodriver.domain.usecase.ObserveOverlayTextSizeUseCase

internal class NarratorOverlayViewModel(
    observeLatestNarratedTelemetryLog: ObserveLatestNarratedTelemetryLogUseCase,
    observeOverlayTextSize: ObserveOverlayTextSizeUseCase,
    observeOverlayBackgroundOpacity: ObserveOverlayBackgroundOpacityUseCase,
) : ViewModel() {
    val uiState: StateFlow<NarratorOverlayUiState> =
        combine(
            observeLatestNarratedTelemetryLog(),
            observeOverlayTextSize(),
            observeOverlayBackgroundOpacity(),
        ) { latestTelemetryLog, overlayTextSize, backgroundOpacity ->
            NarratorOverlayUiState(
                latestTelemetryLog = latestTelemetryLog,
                overlayTextSize = overlayTextSize,
                backgroundOpacity = backgroundOpacity,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NarratorOverlayUiState())
}
