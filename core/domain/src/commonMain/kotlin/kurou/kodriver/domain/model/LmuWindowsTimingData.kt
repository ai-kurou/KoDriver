package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LmuWindowsTimingData(
    val currentLapTimeMs: Long,
    val lastLapTimeMs: Long,
    val bestLapTimeMs: Long,
    val sector1Ms: Long,
    val sector1And2Ms: Long,
    val currentLap: Int,
    val maxLaps: Int,
)
