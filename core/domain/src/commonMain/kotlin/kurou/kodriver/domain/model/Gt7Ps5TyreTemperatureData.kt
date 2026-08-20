package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable

/** GT7 の各タイヤ温度（摂氏）。取得できない場合は 0f。 */
@Serializable
data class Gt7Ps5TyreTemperatureData(
    val frontLeftCelsius: CelsiusReading,
    val frontRightCelsius: CelsiusReading,
    val rearLeftCelsius: CelsiusReading,
    val rearRightCelsius: CelsiusReading,
)
