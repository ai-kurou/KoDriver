package kurou.kodriver.domain.model

data class TelemetryLog(
    val id: Long,
    val createdAt: Long,
    val simulatorId: String,
    val readoutItemKey: String,
    val telemetryJson: String,
)
