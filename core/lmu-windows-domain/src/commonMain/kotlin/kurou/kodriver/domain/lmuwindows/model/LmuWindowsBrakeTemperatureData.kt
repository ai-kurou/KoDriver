package kurou.kodriver.domain.lmuwindows.model

import kotlinx.serialization.Serializable
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.WheelIndex

/**
 * LMU のホイールごとのブレーキ温度。
 *
 * LMU 共有メモリの `LMUWheel.mBrakeTemp` は 1 輪あたり 1 値のみで、
 * タイヤ温度のような表面 / カーカスの区別やローター内外の区別は存在しない。
 * 共有メモリ側は Kelvin のため、Repository 実装側で Celsius へ変換して渡す。
 */
@Serializable
data class LmuWindowsBrakeTemperatureData(
    /** ホイールごとのブレーキ温度（単位: Celsius）。 */
    val wheels: Map<WheelIndex, CelsiusReading>,
)
