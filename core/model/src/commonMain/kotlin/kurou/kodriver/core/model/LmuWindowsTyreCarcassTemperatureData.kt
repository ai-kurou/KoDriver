package kurou.kodriver.core.model

import kotlinx.serialization.Serializable

@Serializable
data class LmuWindowsTyreCarcassTemperatureData(
    /** ホイールごとのカーカス温度（単位: Celsius）。 */
    val wheels: Map<WheelIndex, Double>,
)
