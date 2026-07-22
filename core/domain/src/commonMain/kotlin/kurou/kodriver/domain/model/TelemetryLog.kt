package kurou.kodriver.domain.model

data class TelemetryLog(
    val id: Long,
    val createdAt: Long,
    val simulator: Simulator,
    val readoutItemKey: ReadoutItemKey,
    val telemetryJson: String,
)
