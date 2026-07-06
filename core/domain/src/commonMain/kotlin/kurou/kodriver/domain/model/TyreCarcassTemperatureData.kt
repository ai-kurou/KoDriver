package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TyreCarcassTemperatureData(
    /** ホイールごとのカーカス温度（単位: Celsius）。 */
    val wheels: Map<WheelIndex, Double>,
    /** ホイールごとの表面温度（単位: Celsius）。ログ出力にのみ使用する。 */
    val surfaceWheels: Map<WheelIndex, Double> = emptyMap(),
)
