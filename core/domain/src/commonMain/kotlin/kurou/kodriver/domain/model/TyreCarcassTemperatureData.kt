package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TyreCarcassTemperatureData(
    val wheels: Map<WheelIndex, Double>,
)
