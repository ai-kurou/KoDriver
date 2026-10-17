package kurou.kodriver.domain.lmuwindows.model

import kotlinx.serialization.Serializable
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.WheelIndex

@Serializable
data class LmuWindowsTyreCarcassTemperatureData(
    /** ホイールごとのカーカス温度（単位: Celsius）。 */
    val wheels: Map<WheelIndex, CelsiusReading>,
)
