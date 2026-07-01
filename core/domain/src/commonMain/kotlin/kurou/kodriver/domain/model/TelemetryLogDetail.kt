package kurou.kodriver.domain.model

data class TelemetryLogDetail(
    val current: TelemetryLog,
    val previous: TelemetryLog?,
)
