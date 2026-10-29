package kurou.kodriver.domain.lmuwindows.model

import kotlinx.serialization.Serializable

@Serializable
data class LmuWindowsEngineData(
    val rpm: Double,
    val maxRpm: Double,
    val gear: Int,
)
