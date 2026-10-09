package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable

/**
 * LMU のホイールごとのブレーキ残り厚さ。
 *
 * 共有メモリには含まれず、LMU 内蔵 REST API の `wearables.brakes` から取得する。
 */
@Serializable
data class LmuWindowsBrakeWearData(
    /** ホイールごとのブレーキ残り厚さ（単位: meters）。 */
    val wheels: Map<WheelIndex, BrakeThicknessMeters>,
)
