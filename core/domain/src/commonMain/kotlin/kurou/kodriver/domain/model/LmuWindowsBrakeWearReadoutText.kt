package kurou.kodriver.domain.model

/** ブレーキ残量警告文言の閾値プレースホルダーを置換する。 */
fun formatLmuWindowsBrakeWearReadoutText(
    template: String,
    percent: Int,
): String = template.replace(LMU_WINDOWS_BRAKE_WEAR_PERCENT_PLACEHOLDER, percent.toString())

/** 文言内の `{...}` のうち、`{percent}` 以外のトークンを出現順・重複なしで返す。 */
fun findUnknownLmuWindowsBrakeWearReadoutPlaceholders(template: String): List<String> =
    Regex("\\{[^{}]*\\}")
        .findAll(template)
        .map { it.value }
        .filter { it != LMU_WINDOWS_BRAKE_WEAR_PERCENT_PLACEHOLDER }
        .distinct()
        .toList()
