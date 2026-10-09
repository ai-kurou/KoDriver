package kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail

import kotlin.math.abs
import kotlin.math.roundToLong

private const val FRACTION_DIGITS = 8
private const val SCALE = 100_000_000L

/** 生の値の小さな変化も読み取れるよう、指数表記にならない固定小数点（小数8桁）で整形する。 */
internal fun formatBrakeWearValue(value: Double): String {
    val scaled = (abs(value) * SCALE).roundToLong()
    val sign = if (value < 0.0 && scaled != 0L) "-" else ""
    val fraction = (scaled % SCALE).toString().padStart(FRACTION_DIGITS, '0')
    return "$sign${scaled / SCALE}.$fraction"
}

/** [formatBrakeWearValue] に、増加が分かる `+` を付ける（0 に丸まる場合は付けない）。 */
internal fun formatBrakeWearDelta(delta: Double): String {
    val formatted = formatBrakeWearValue(delta)
    return if (delta > 0.0 && formatted != formatBrakeWearValue(0.0)) "+$formatted" else formatted
}
