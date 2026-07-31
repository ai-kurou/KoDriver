package kurou.kodriver.core.designsystem

import kotlin.math.abs
import kotlin.math.roundToLong

private val INT_PLACEHOLDER_REGEX = Regex("""%1?\$?[ds]""")
private val DECIMAL_PLACEHOLDER_REGEX = Regex("""%1?\$?\.(\d+)f""")

/**
 * strings.xml の printf 形式プレースホルダー（`%1$d`・`%1$s` 相当）を含むテンプレート文字列へ整数値を埋め込む。
 * commonMain からは `java.util.Formatter` 経由の `String.format` を利用できないため、
 * 本アプリのスライダーラベルで実際に使用しているプレースホルダー・エスケープ（`%%` → `%`）のみを
 * サポートする簡易実装。
 */
fun String.formatSliderLabel(value: Int): String =
    INT_PLACEHOLDER_REGEX.replace(this) { value.toString() }.unescapePercent()

/**
 * strings.xml の printf 形式プレースホルダー（`%1$.1f` 相当）を含むテンプレート文字列へ小数値を埋め込む。
 */
fun String.formatSliderLabel(value: Float): String =
    DECIMAL_PLACEHOLDER_REGEX.replace(this) { match ->
        formatFixedPoint(value, decimals = match.groupValues[1].toInt())
    }.unescapePercent()

private fun String.unescapePercent(): String = replace("%%", "%")

private fun formatFixedPoint(value: Float, decimals: Int): String {
    if (decimals <= 0) return value.roundToLong().toString()
    var factor = 1L
    repeat(decimals) { factor *= 10 }
    val scaled = (value.toDouble() * factor).roundToLong()
    val sign = if (scaled < 0) "-" else ""
    val absScaled = abs(scaled)
    val whole = absScaled / factor
    val fraction = (absScaled % factor).toString().padStart(decimals, '0')
    return "$sign$whole.$fraction"
}
