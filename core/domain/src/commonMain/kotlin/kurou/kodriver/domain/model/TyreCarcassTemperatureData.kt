package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TyreCarcassTemperatureData(
    /** ホイールごとのカーカス温度（単位: Celsius）。 */
    val wheels: Map<WheelIndex, Double>,
)
