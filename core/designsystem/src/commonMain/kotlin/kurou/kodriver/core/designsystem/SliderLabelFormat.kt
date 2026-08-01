package kurou.kodriver.core.designsystem

import kotlin.math.abs
import kotlin.math.roundToLong

private val INT_PLACEHOLDER_REGEX = Regex("""%1?\$?[ds]""")
private val DECIMAL_PLACEHOLDER_REGEX = Regex("""%1?\$?\.(\d+)f""")

// 対応済みのスライダーラベル用プレースホルダーを置換した後に、未処理の printf 風トークンだけを検出する。
// これは Formatter 互換の検証ではなく、`%2$d` や `%1$02.1f` などを画面表示へ残さないためのガード。
private val PRINTF_PLACEHOLDER_REGEX = Regex("""%(?!%)(?:\d+\$)?[#+ 0,(<-]*\d*(?:\.\d+)?[a-zA-Z]""")

/**
 * strings.xml の printf 形式プレースホルダー（`%1$d`・`%1$s` 相当）を含むテンプレート文字列へ整数値を埋め込む。
 * commonMain からは `java.util.Formatter` 経由の `String.format` を利用できないため、
 * 本アプリのスライダーラベルで実際に使用しているプレースホルダー・エスケープ（`%%` → `%`）のみを
 * サポートする簡易実装。
 */
fun String.formatSliderLabel(value: Int): String =
    replaceSupportedPlaceholders(INT_PLACEHOLDER_REGEX) { value.toString() }

/**
 * strings.xml の printf 形式プレースホルダー（`%1$.1f` 相当）を含むテンプレート文字列へ小数値を埋め込む。
 */
fun String.formatSliderLabel(value: Float): String =
    replaceSupportedPlaceholders(DECIMAL_PLACEHOLDER_REGEX) { match ->
        formatFixedPoint(value, decimals = match.groupValues[1].toInt())
    }

private fun String.replaceSupportedPlaceholders(
    supportedPlaceholderRegex: Regex,
    replacement: (MatchResult) -> String,
): String {
    val template = this
    val formatted = StringBuilder()
    var index = 0

    while (index < template.length) {
        if (template.startsWith("%%", startIndex = index)) {
            formatted.append('%')
            index += 2
            continue
        }

        val supportedPlaceholder = supportedPlaceholderRegex.matchAt(template, index)
        if (supportedPlaceholder != null) {
            formatted.append(replacement(supportedPlaceholder))
            index = supportedPlaceholder.range.last + 1
            continue
        }

        val unsupportedPlaceholder = PRINTF_PLACEHOLDER_REGEX.matchAt(template, index)?.value
        require(unsupportedPlaceholder == null) {
            "Unsupported slider label placeholder: $unsupportedPlaceholder in template: $template"
        }

        formatted.append(template[index])
        index++
    }

    return formatted.toString()
}

/**
 * [value] を四捨五入（half-up、`String.format` の `%f` と同じ丸め方向）して [decimals] 桁の
 * 固定小数点表記へ変換する。小数点は常に `.`（ロケール非依存）を用いる。
 * `NaN`・`Infinity`・`-Infinity` は Kotlin 標準の [Float.toString] と同じ表記を返す。
 */
private fun formatFixedPoint(
    value: Float,
    decimals: Int,
): String {
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
