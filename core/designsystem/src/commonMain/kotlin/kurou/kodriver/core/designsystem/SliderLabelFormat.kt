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
    DECIMAL_PLACEHOLDER_REGEX
        .replace(this) { match ->
        formatFixedPoint(value, decimals = match.groupValues[1].toInt())
    }.unescapePercent()

private fun String.unescapePercent(): String = replace("%%", "%")

/**
 * [value] を四捨五入（half-up、`String.format` の `%f` と同じ丸め方向）して [decimals] 桁の
 * 固定小数点表記へ変換する。小数点は常に `.`（ロケール非依存）を用いる。
 * `NaN`・`Infinity`・`-Infinity` は Kotlin 標準の [Float.toString] と同じ表記を返す。
 */
private fun formatFixedPoint(value: Float, decimals: Int): String {
    if (value.isNaN()) return "NaN"
    if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"
    val sign = if (value < 0) "-" else ""
    val absValue = abs(value.toDouble())
    if (decimals <= 0) return "$sign${absValue.roundToLong()}"
    var factor = 1L
    repeat(decimals) { factor *= 10 }
    // absValue は常に 0 以上のため、roundToLong() の「0.5 は正の無限大方向へ丸める」動作が
    // そのまま half-up（0 から遠ざかる方向への四捨五入）と一致する。
    val absScaled = (absValue * factor).roundToLong()
    val whole = absScaled / factor
    val fraction = (absScaled % factor).toString().padStart(decimals, '0')
    return "$sign$whole.$fraction"
}
