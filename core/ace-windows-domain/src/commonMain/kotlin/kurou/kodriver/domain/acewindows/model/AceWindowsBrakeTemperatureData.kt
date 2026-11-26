package kurou.kodriver.domain.acewindows.model

import kotlinx.serialization.Serializable
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.WheelIndex

/**
 * Assetto Corsa EVO の Windows 共有メモリから読み取ったブレーキ温度。
 */
@Serializable
public data class AceWindowsBrakeTemperatureData(
    /** ホイールごとのブレーキ温度（単位: Celsius）。 */
    val wheels: Map<WheelIndex, CelsiusReading>,
)
