package kurou.kodriver.domain.telemetrylog.model

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator

public data class TelemetryLog(
    val id: Long,
    val createdAt: Long,
    val simulator: Simulator,
    val readoutItemKey: ReadoutItemKey,
    val narratedText: String,
    val narrationOutcome: NarrationOutcome,
    val narrationSource: NarrationSource,
    val telemetryJson: String,
)
