package kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail

import kurou.kodriver.domain.model.BrakeThicknessMeters
import kotlin.math.roundToLong

private const val MILLIMETERS_PER_METER = 1000.0
private const val THOUSANDTHS_PER_MILLIMETER = 1000L
private const val THOUSANDTHS_DIGITS = 3
private const val TENTHS_PER_UNIT = 10L

/** ブレーキの厚さをミリメートル（小数3桁）で整形する。負の値は 0 として扱う。微小な摩耗を確認できるよう細かく出す。 */
internal fun formatBrakeThicknessMillimeters(thickness: BrakeThicknessMeters): String {
    val thousandths =
        (thickness.value.toDouble() * MILLIMETERS_PER_METER * THOUSANDTHS_PER_MILLIMETER).roundToLong()
    val clamped = thousandths.coerceAtLeast(0L)
    val fraction = (clamped % THOUSANDTHS_PER_MILLIMETER).toString().padStart(THOUSANDTHS_DIGITS, '0')
    return "${clamped / THOUSANDTHS_PER_MILLIMETER}.$fraction"
}

/** 残量%を小数1桁で整形する。 */
internal fun formatBrakeWearPercent(percent: Float): String {
    val tenths = (percent.toDouble() * TENTHS_PER_UNIT).roundToLong().coerceAtLeast(0L)
    return "${tenths / TENTHS_PER_UNIT}.${tenths % TENTHS_PER_UNIT}%"
}
