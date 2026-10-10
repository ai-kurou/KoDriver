package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable

/** 1輪分のブレーキ残量。[remainingPercent] は 0〜100（小数を含む）。 */
@Serializable
data class LmuWindowsBrakeWearWheelRemaining(
    val thickness: BrakeThicknessMeters,
    val remainingPercent: Float,
)

/** ホイールごとのブレーキ残量。 */
@Serializable
data class LmuWindowsBrakeWearRemainingData(
    val wheels: Map<WheelIndex, LmuWindowsBrakeWearWheelRemaining>,
)

private const val PERCENT_MAX = 100f

/**
 * 現在の厚さが「新品時の厚さ [maxThickness]」から「破損厚さ [failureThickness]」までの間のどこにあるかを % で返す。
 * 使える厚さの幅が 0 以下（新品時の厚さが破損厚さ以下）の場合は比較できないため 100 を返す。
 */
fun calculateBrakeWearRemainingPercent(
    current: BrakeThicknessMeters,
    maxThickness: BrakeThicknessMeters,
    failureThickness: BrakeThicknessMeters,
): Float {
    val usable = maxThickness.value - failureThickness.value
    if (usable <= 0f) return PERCENT_MAX
    val fraction = (current.value - failureThickness.value) / usable
    return (fraction * PERCENT_MAX).coerceIn(0f, PERCENT_MAX)
}
