package kurou.kodriver.domain.acewindows.model

import kotlinx.serialization.Serializable
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.WheelIndex

/**
 * Assetto Corsa EVO の Windows 共有メモリから読み取ったタイヤのカーカス平均温度。
 */
@Serializable
data class AceWindowsTyreCarcassTemperatureData(
    /** ホイールごとのカーカス平均温度（単位: Celsius）。 */
    val wheels: Map<WheelIndex, CelsiusReading>,
)
