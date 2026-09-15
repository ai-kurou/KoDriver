package kurou.kodriver.feature.narratoroverlay

import kurou.kodriver.domain.model.TelemetryLog

internal data class NarratorOverlayUiState(
    val latestTelemetryLog: TelemetryLog? = null,
)
