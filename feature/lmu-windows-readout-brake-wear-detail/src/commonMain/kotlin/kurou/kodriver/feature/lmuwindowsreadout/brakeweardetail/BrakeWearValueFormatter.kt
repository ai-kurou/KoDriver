package kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail

import kurou.kodriver.domain.model.BrakeThicknessMeters
import kotlin.math.roundToLong

private const val MILLIMETERS_PER_METER = 1000.0
private const val TENTHS_PER_MILLIMETER = 10L

/** ブレーキの厚さをミリメートル（小数1桁）で整形する。負の値は 0 として扱う。 */
internal fun formatBrakeThicknessMillimeters(thickness: BrakeThicknessMeters): String {
    val tenths = (thickness.value.toDouble() * MILLIMETERS_PER_METER * TENTHS_PER_MILLIMETER).roundToLong()
    val clamped = tenths.coerceAtLeast(0L)
    return "${clamped / TENTHS_PER_MILLIMETER}.${clamped % TENTHS_PER_MILLIMETER}"
}

internal fun formatBrakeWearPercent(percent: Int): String = "$percent%"
