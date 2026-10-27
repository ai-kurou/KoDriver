package kurou.kodriver.feature.narratoroverlay

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.model.OverlayTextSize
import kurou.kodriver.domain.model.TelemetryLog

/** 最新の読み上げログと表示設定を画面状態へ合成する冷たい Flow。 */
internal class NarratorOverlayFlow(
    latestTelemetryLog: Flow<TelemetryLog?>,
    overlayTextSize: Flow<OverlayTextSize>,
    overlayTextColor: Flow<Long>,
    backgroundOpacity: Flow<Int>,
) {
    val uiState: Flow<NarratorOverlayUiState> =
        combine(
            latestTelemetryLog,
            overlayTextSize,
            overlayTextColor,
            backgroundOpacity,
        ) { latestTelemetryLog, overlayTextSize, overlayTextColor, backgroundOpacity ->
            NarratorOverlayUiState(
                latestTelemetryLog = latestTelemetryLog,
                overlayTextSize = overlayTextSize,
                overlayTextColor = overlayTextColor,
                backgroundOpacity = backgroundOpacity,
            )
        }
}
