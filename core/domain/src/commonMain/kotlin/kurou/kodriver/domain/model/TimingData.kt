package kurou.kodriver.domain.model

data class TimingData(
    val currentLapTimeMs: Long,
    val lastLapTimeMs: Long,
    val bestLapTimeMs: Long,
    val sector1Ms: Long,
    val sector2Ms: Long,
    val currentLap: Int,
)
