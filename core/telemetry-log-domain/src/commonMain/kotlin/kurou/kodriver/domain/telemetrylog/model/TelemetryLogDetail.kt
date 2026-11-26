package kurou.kodriver.domain.telemetrylog.model

public data class TelemetryLogDetail(
    val current: TelemetryLog,
    val previous: TelemetryLog?,
)
