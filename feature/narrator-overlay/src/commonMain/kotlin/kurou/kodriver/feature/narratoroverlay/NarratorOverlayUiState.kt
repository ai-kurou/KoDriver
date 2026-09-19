package kurou.kodriver.feature.narratoroverlay

import kurou.kodriver.domain.model.OVERLAY_BACKGROUND_OPACITY_DEFAULT
import kurou.kodriver.domain.model.OVERLAY_TEXT_SIZE_DEFAULT
import kurou.kodriver.domain.model.OverlayTextSize
import kurou.kodriver.domain.model.TelemetryLog

internal data class NarratorOverlayUiState(
    val latestTelemetryLog: TelemetryLog? = null,
    val overlayTextSize: OverlayTextSize = OVERLAY_TEXT_SIZE_DEFAULT,
    val backgroundOpacity: Int = OVERLAY_BACKGROUND_OPACITY_DEFAULT,
)
