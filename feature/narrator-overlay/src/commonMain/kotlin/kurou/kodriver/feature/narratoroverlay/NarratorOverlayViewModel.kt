package kurou.kodriver.feature.narratoroverlay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.overlay.usecase.ObserveOverlayBackgroundOpacityUseCase
import kurou.kodriver.domain.overlay.usecase.ObserveOverlayTextColorUseCase
import kurou.kodriver.domain.overlay.usecase.ObserveOverlayTextSizeUseCase
import kurou.kodriver.domain.speechrecognition.usecase.ObserveSpeechRecognitionTranscriptUseCase
import kurou.kodriver.domain.usecase.ObserveLatestNarratedTelemetryLogUseCase

internal class NarratorOverlayViewModel(
    observeLatestNarratedTelemetryLog: ObserveLatestNarratedTelemetryLogUseCase,
    observeOverlayTextSize: ObserveOverlayTextSizeUseCase,
    observeOverlayTextColor: ObserveOverlayTextColorUseCase,
    observeOverlayBackgroundOpacity: ObserveOverlayBackgroundOpacityUseCase,
    observeSpeechRecognitionTranscript: ObserveSpeechRecognitionTranscriptUseCase,
) : ViewModel() {
    val uiState: StateFlow<NarratorOverlayUiState> =
        NarratorOverlayFlow(
            latestTelemetryLog = observeLatestNarratedTelemetryLog(),
            overlayTextSize = observeOverlayTextSize(),
            overlayTextColor = observeOverlayTextColor(),
            backgroundOpacity = observeOverlayBackgroundOpacity(),
            speechRecognitionTranscript = observeSpeechRecognitionTranscript(),
        ).uiState.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NarratorOverlayUiState())
}
