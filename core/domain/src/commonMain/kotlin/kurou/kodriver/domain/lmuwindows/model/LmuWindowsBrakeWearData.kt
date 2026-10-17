package kurou.kodriver.domain.lmuwindows.model

import kotlinx.serialization.Serializable
import kurou.kodriver.domain.model.BrakeThicknessMeters

/**
 * LMU のホイールごとのブレーキ残り厚さ。
 *
 * 共有メモリには含まれず、LMU 内蔵 REST API の `wearables.brakes` から取得する。
 * Android 版は KoDriver サーバーの `/ws/lmu_windows/brake_wear` から受信する。
 */
@Serializable
data class LmuWindowsBrakeWearData(
    /** ホイールごとのブレーキ残り厚さ（単位: meters）。 */
    val wheels: Map<WheelIndex, BrakeThicknessMeters>,
)
