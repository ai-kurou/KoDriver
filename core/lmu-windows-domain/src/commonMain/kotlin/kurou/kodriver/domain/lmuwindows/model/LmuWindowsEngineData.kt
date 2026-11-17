package kurou.kodriver.domain.lmuwindows.model

import kotlinx.serialization.Serializable

@Serializable
public data class LmuWindowsEngineData(
    val rpm: Double,
    val maxRpm: Double,
    val gear: Int,
)
