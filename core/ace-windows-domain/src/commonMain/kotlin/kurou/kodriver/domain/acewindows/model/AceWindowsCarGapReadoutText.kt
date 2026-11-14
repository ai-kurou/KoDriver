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
 * 秒数は符号を無視して小数1桁に丸める。0（小数1桁で 0.0 になる値を含む）・非有限値は「前方車なし」「後方車なし」にする。
 */
fun formatAceWindowsCarGapReadoutText(
    template: String,
    data: AceWindowsGapData,
): String =
    template
        .replace(
            ACE_WINDOWS_CAR_GAP_AHEAD_PLACEHOLDER,
            carGapPhrase("前方", data.gapAheadSeconds, ACE_WINDOWS_CAR_GAP_AHEAD_NONE_TEXT),
        ).replace(
            ACE_WINDOWS_CAR_GAP_BEHIND_PLACEHOLDER,
            carGapPhrase("後方", data.gapBehindSeconds, ACE_WINDOWS_CAR_GAP_BEHIND_NONE_TEXT),
        )

private fun carGapPhrase(
    label: String,
    seconds: Float,
    noneText: String,
): String {
    if (!seconds.isFinite()) return noneText
    val tenths = (abs(seconds.toDouble()) * CAR_GAP_DECIMAL_SCALE).roundToLong()
    if (tenths == 0L) return noneText
    return "$label${tenths / CAR_GAP_DECIMAL_DIVISOR}.${tenths % CAR_GAP_DECIMAL_DIVISOR}秒"
}

/** 文言内の `{...}` のうち、`{ahead}` `{behind}` 以外のトークンを出現順・重複なしで返す。 */
fun findUnknownAceWindowsCarGapReadoutPlaceholders(template: String): List<String> =
    CAR_GAP_PLACEHOLDER_TOKEN_REGEX
        .findAll(template)
        .map { it.value }
        .filter { it !in CAR_GAP_PLACEHOLDERS }
        .distinct()
        .toList()
