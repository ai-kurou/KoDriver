package kurou.kodriver.domain.acewindows.model

import kotlin.math.abs
import kotlin.math.roundToLong

private const val CAR_GAP_DECIMAL_SCALE = 10.0
private const val CAR_GAP_DECIMAL_DIVISOR = 10L
private val CAR_GAP_PLACEHOLDER_TOKEN_REGEX = Regex("\\{[^{}]*\\}")
private val CAR_GAP_PLACEHOLDERS =
    setOf(ACE_WINDOWS_CAR_GAP_AHEAD_PLACEHOLDER, ACE_WINDOWS_CAR_GAP_BEHIND_PLACEHOLDER)

/**
 * 前後車ギャップ文言の `{ahead}` `{behind}` を置換する。
 * 秒数は符号を無視して小数1桁の単位なし数値にする。
 * 0（小数1桁で 0.0 になる値を含む）・非有限値のときは、まず「前方{ahead}秒」「後方{behind}秒」の節全体を
 * 「前方車なし」「後方車なし」に置換し、節を含まないプレースホルダーは「なし」に置換する。
 */
fun formatAceWindowsCarGapReadoutText(
    template: String,
    data: AceWindowsGapData,
): String {
    val ahead = carGapSeconds(data.gapAheadSeconds)
    val behind = carGapSeconds(data.gapBehindSeconds)
    return template
        .replaceGap(
            clause = ACE_WINDOWS_CAR_GAP_AHEAD_CLAUSE,
            placeholder = ACE_WINDOWS_CAR_GAP_AHEAD_PLACEHOLDER,
            seconds = ahead,
            noneClauseText = ACE_WINDOWS_CAR_GAP_AHEAD_NONE_TEXT,
        ).replaceGap(
            clause = ACE_WINDOWS_CAR_GAP_BEHIND_CLAUSE,
            placeholder = ACE_WINDOWS_CAR_GAP_BEHIND_PLACEHOLDER,
            seconds = behind,
            noneClauseText = ACE_WINDOWS_CAR_GAP_BEHIND_NONE_TEXT,
        )
}

private fun String.replaceGap(
    clause: String,
    placeholder: String,
    seconds: String?,
    noneClauseText: String,
): String {
    if (seconds != null) return replace(placeholder, seconds)
    return replace(clause, noneClauseText).replace(placeholder, ACE_WINDOWS_CAR_GAP_NONE_VALUE_TEXT)
}

/** 符号を無視して小数1桁に丸めた秒数。ギャップなし（0・非有限）のときは null。 */
private fun carGapSeconds(seconds: Float): String? {
    if (!seconds.isFinite()) return null
    val tenths = (abs(seconds.toDouble()) * CAR_GAP_DECIMAL_SCALE).roundToLong()
    if (tenths == 0L) return null
    return "${tenths / CAR_GAP_DECIMAL_DIVISOR}.${tenths % CAR_GAP_DECIMAL_DIVISOR}"
}

/** 文言内の `{...}` のうち、`{ahead}` `{behind}` 以外のトークンを出現順・重複なしで返す。 */
fun findUnknownAceWindowsCarGapReadoutPlaceholders(template: String): List<String> =
    CAR_GAP_PLACEHOLDER_TOKEN_REGEX
        .findAll(template)
        .map { it.value }
        .filter { it !in CAR_GAP_PLACEHOLDERS }
        .distinct()
        .toList()
