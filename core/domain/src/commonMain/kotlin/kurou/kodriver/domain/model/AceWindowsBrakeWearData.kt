package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable

/**
 * Assetto Corsa EVO の Windows 共有メモリから読み取ったブレーキパッド・ディスクの摩耗指標。
 *
 * `padLife` / `discLife` は 1.0（新品）から減少していく相対値で、絶対スケールは未較正
 * （docs/ace-windows-telemetry.md 参照）。タイヤ摩耗（`tyreWear`）は常に0.0で取得不可のため、
 * 代替の摩耗指標として本値を用いる。
 */
@Serializable
data class AceWindowsBrakeWearData(
    /** ホイールごとのブレーキパッド残量（1.0=新品からの相対値）。 */
    val padLife: Map<WheelIndex, Double>,
    /** ホイールごとのブレーキディスク残量（1.0=新品からの相対値）。 */
    val discLife: Map<WheelIndex, Double>,
)
