package kurou.kodriver.domain.model

data class Gt7Ps5TelemetryData(
    val lapCount: Int,
    val lapsInRace: Int,
    val bestLapTimeMs: Int,
    val gasLevel: Float,
    val gasCapacity: Float,
)
