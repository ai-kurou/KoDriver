package kurou.kodriver.domain.lmuwindows.model

import kotlin.math.roundToLong

private const val CLASS_CAR_GAP_DECIMAL_SCALE = 10.0
private const val CLASS_CAR_GAP_DECIMAL_DIVISOR = 10L
private val CLASS_CAR_GAP_PLACEHOLDER_TOKEN_REGEX = Regex("\\{[^{}]*\\}")
private val CLASS_CAR_GAP_KNOWN_PLACEHOLDERS =
    setOf(LMU_WINDOWS_CLASS_CAR_GAP_AHEAD_PLACEHOLDER, LMU_WINDOWS_CLASS_CAR_GAP_BEHIND_PLACEHOLDER)

/** 秒差を小数1桁に丸めた単位なしの文字列にする。 */
fun formatClassCarGapSeconds(seconds: Double): String {
    val tenths = (seconds * CLASS_CAR_GAP_DECIMAL_SCALE).roundToLong()
    return "${tenths / CLASS_CAR_GAP_DECIMAL_DIVISOR}.${tenths % CLASS_CAR_GAP_DECIMAL_DIVISOR}"
}

/**
 * クラス内ギャップ文言の `{ahead}` `{behind}` を置換する。
 * [aheadSeconds]・[behindSeconds] は小数1桁の単位なし秒数で、null は車なし（先頭・最後尾）を表す。
 * 車なしのときは、まず「前方{ahead}秒」「後方{behind}秒」の節全体を「クラス内先頭です」「クラス内最後尾です」に置換し、
 * 節を含まないプレースホルダーは「なし」に置換する。
 */
fun formatLmuWindowsClassCarGapReadoutText(
    template: String,
    aheadSeconds: String?,
    behindSeconds: String?,
): String =
    template
        .replaceGap(
            clause = LMU_WINDOWS_CLASS_CAR_GAP_AHEAD_CLAUSE,
            placeholder = LMU_WINDOWS_CLASS_CAR_GAP_AHEAD_PLACEHOLDER,
            seconds = aheadSeconds,
            noneClauseText = LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_TOP_TEXT_DEFAULT,
        ).replaceGap(
            clause = LMU_WINDOWS_CLASS_CAR_GAP_BEHIND_CLAUSE,
            placeholder = LMU_WINDOWS_CLASS_CAR_GAP_BEHIND_PLACEHOLDER,
            seconds = behindSeconds,
            noneClauseText = LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_LAST_TEXT_DEFAULT,
        )

private fun String.replaceGap(
    clause: String,
    placeholder: String,
    seconds: String?,
    noneClauseText: String,
): String {
    if (seconds != null) return replace(placeholder, seconds)
    return replace(clause, noneClauseText).replace(placeholder, LMU_WINDOWS_CLASS_CAR_GAP_NONE_VALUE_TEXT)
}

/** プレビュー用に、前方・後方とも同じ秒差の文言を作る。 */
fun formatLmuWindowsClassCarGapPreviewText(
    template: String,
    seconds: Double,
): String {
    val text = formatClassCarGapSeconds(seconds)
    return formatLmuWindowsClassCarGapReadoutText(template, aheadSeconds = text, behindSeconds = text)
}

/** 文言内の `{...}` のうち、`{ahead}`・`{behind}` 以外のトークンを出現順・重複なしで返す。 */
fun findUnknownLmuWindowsClassCarGapReadoutPlaceholders(template: String): List<String> =
    CLASS_CAR_GAP_PLACEHOLDER_TOKEN_REGEX
        .findAll(template)
        .map { it.value }
        .filter { it !in CLASS_CAR_GAP_KNOWN_PLACEHOLDERS }
        .distinct()
        .toList()
