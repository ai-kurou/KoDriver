package kurou.kodriver.feature.narratoroverlay

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.overlay.model.OverlayTextSize
import kurou.kodriver.domain.speechrecognition.model.SpeechRecognitionTranscript

/** 最新の読み上げログ・表示設定・マイクが聞き取った文言を画面状態へ合成する冷たい Flow。 */
internal class NarratorOverlayFlow(
    latestTelemetryLog: Flow<TelemetryLog?>,
    overlayTextSize: Flow<OverlayTextSize>,
    overlayTextColor: Flow<Long>,
    backgroundOpacity: Flow<Int>,
    speechRecognitionTranscript: Flow<SpeechRecognitionTranscript>,
) {
    val uiState: Flow<NarratorOverlayUiState> =
        combine(
            latestTelemetryLog,
            overlayTextSize,
            overlayTextColor,
            backgroundOpacity,
            speechRecognitionTranscript,
        ) { latestTelemetryLog, overlayTextSize, overlayTextColor, backgroundOpacity, speechRecognitionTranscript ->
            NarratorOverlayUiState(
                latestTelemetryLog = latestTelemetryLog,
                overlayTextSize = overlayTextSize,
                overlayTextColor = overlayTextColor,
                backgroundOpacity = backgroundOpacity,
                speechRecognitionTranscript = speechRecognitionTranscript,
            )
        }
}
