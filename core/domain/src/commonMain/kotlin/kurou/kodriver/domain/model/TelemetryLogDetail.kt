package kurou.kodriver.domain.model

public data class TelemetryLogDetail(
    val current: TelemetryLog,
    val previous: TelemetryLog?,
)
