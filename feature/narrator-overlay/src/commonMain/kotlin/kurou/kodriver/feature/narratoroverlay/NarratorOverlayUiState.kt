package kurou.kodriver.feature.narratoroverlay

import kurou.kodriver.domain.overlay.model.OVERLAY_BACKGROUND_OPACITY_DEFAULT
import kurou.kodriver.domain.overlay.model.OVERLAY_TEXT_COLOR_DEFAULT
import kurou.kodriver.domain.overlay.model.OVERLAY_TEXT_SIZE_DEFAULT
import kurou.kodriver.domain.overlay.model.OverlayTextSize
import kurou.kodriver.domain.speechrecognition.model.SpeechRecognitionTranscript
import kurou.kodriver.domain.telemetrylog.model.TelemetryLog

internal data class NarratorOverlayUiState(
    val latestTelemetryLog: TelemetryLog? = null,
    val overlayTextSize: OverlayTextSize = OVERLAY_TEXT_SIZE_DEFAULT,
    val overlayTextColor: Long = OVERLAY_TEXT_COLOR_DEFAULT,
    val backgroundOpacity: Int = OVERLAY_BACKGROUND_OPACITY_DEFAULT,
    val speechRecognitionTranscript: SpeechRecognitionTranscript = SpeechRecognitionTranscript(),
)
