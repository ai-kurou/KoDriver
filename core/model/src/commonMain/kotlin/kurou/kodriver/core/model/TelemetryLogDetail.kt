package kurou.kodriver.core.model

data class TelemetryLogDetail(
    val current: TelemetryLog,
    val previous: TelemetryLog?,
)
