package kurou.kodriver.domain.lmuwindows.model

import kotlin.math.roundToLong

private const val CLASS_CAR_GAP_DECIMAL_SCALE = 10.0
private const val CLASS_CAR_GAP_DECIMAL_DIVISOR = 10L
private const val CLASS_CAR_GAP_SECONDS_TOKEN = "{seconds}"
private val CLASS_CAR_GAP_PLACEHOLDER_TOKEN_REGEX = Regex("\\{[^{}]*\\}")
private val CLASS_CAR_GAP_KNOWN_PLACEHOLDERS =
    setOf(LMU_WINDOWS_CLASS_CAR_GAP_AHEAD_PLACEHOLDER, LMU_WINDOWS_CLASS_CAR_GAP_BEHIND_PLACEHOLDER)

/** 秒差を小数1桁に丸めた文字列にする。 */
private fun formatClassCarGapSeconds(seconds: Double): String {
    val tenths = (seconds * CLASS_CAR_GAP_DECIMAL_SCALE).roundToLong()
    return "${tenths / CLASS_CAR_GAP_DECIMAL_DIVISOR}.${tenths % CLASS_CAR_GAP_DECIMAL_DIVISOR}"
}

/** 前方車とのギャップ部分の文。秒数は小数1桁に丸める。 */
fun formatLmuWindowsClassCarGapAheadPhrase(seconds: Double): String =
    LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_AHEAD_PHRASE.replace(
        CLASS_CAR_GAP_SECONDS_TOKEN,
        formatClassCarGapSeconds(seconds),
    )

/** 後方車とのギャップ部分の文。秒数は小数1桁に丸める。 */
fun formatLmuWindowsClassCarGapBehindPhrase(seconds: Double): String =
    LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_BEHIND_PHRASE.replace(
        CLASS_CAR_GAP_SECONDS_TOKEN,
        formatClassCarGapSeconds(seconds),
    )

/** クラス内ギャップ文言の `{ahead}` と `{behind}` を、それぞれの部分の文に置換する。 */
fun formatLmuWindowsClassCarGapReadoutText(
    template: String,
    aheadPhrase: String,
    behindPhrase: String,
): String =
    template
        .replace(LMU_WINDOWS_CLASS_CAR_GAP_AHEAD_PLACEHOLDER, aheadPhrase)
        .replace(LMU_WINDOWS_CLASS_CAR_GAP_BEHIND_PLACEHOLDER, behindPhrase)

/** プレビュー用に、前方・後方とも同じ秒差の文言を作る。 */
fun formatLmuWindowsClassCarGapPreviewText(
    template: String,
    seconds: Double,
): String =
    formatLmuWindowsClassCarGapReadoutText(
        template,
        formatLmuWindowsClassCarGapAheadPhrase(seconds),
        formatLmuWindowsClassCarGapBehindPhrase(seconds),
    )

/** 文言内の `{...}` のうち、`{ahead}`・`{behind}` 以外のトークンを出現順・重複なしで返す。 */
fun findUnknownLmuWindowsClassCarGapReadoutPlaceholders(template: String): List<String> =
    CLASS_CAR_GAP_PLACEHOLDER_TOKEN_REGEX
        .findAll(template)
        .map { it.value }
        .filter { it !in CLASS_CAR_GAP_KNOWN_PLACEHOLDERS }
        .distinct()
        .toList()
