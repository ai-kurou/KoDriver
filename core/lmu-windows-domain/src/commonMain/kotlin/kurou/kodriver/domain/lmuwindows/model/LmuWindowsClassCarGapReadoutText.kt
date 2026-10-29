package kurou.kodriver.domain.lmuwindows.model

import kotlin.math.roundToLong

private const val CLASS_CAR_GAP_DECIMAL_SCALE = 10.0
private const val CLASS_CAR_GAP_DECIMAL_DIVISOR = 10L

/** クラス内ギャップ文言の秒数プレースホルダーを小数1桁に丸めた値で置換する。 */
fun formatLmuWindowsClassCarGapReadoutText(
    template: String,
    seconds: Double,
): String {
    val tenths = (seconds * CLASS_CAR_GAP_DECIMAL_SCALE).roundToLong()
    val formatted = "${tenths / CLASS_CAR_GAP_DECIMAL_DIVISOR}.${tenths % CLASS_CAR_GAP_DECIMAL_DIVISOR}"
    return template.replace(LMU_WINDOWS_CLASS_CAR_GAP_SECONDS_PLACEHOLDER, formatted)
}

/** 文言内の `{...}` のうち、`{seconds}` 以外のトークンを出現順・重複なしで返す。 */
fun findUnknownLmuWindowsClassCarGapReadoutPlaceholders(template: String): List<String> =
    Regex("\\{[^{}]*\\}")
        .findAll(template)
        .map { it.value }
        .filter { it != LMU_WINDOWS_CLASS_CAR_GAP_SECONDS_PLACEHOLDER }
        .distinct()
        .toList()
