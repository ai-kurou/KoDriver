package kurou.kodriver.domain.model

/** タイヤ残存率警告文言の閾値プレースホルダーを置換する。 */
fun formatLmuWindowsTyreWearReadoutText(
    template: String,
    percentage: Int,
): String = template.replace(LMU_WINDOWS_TYRE_WEAR_PERCENT_PLACEHOLDER, percentage.toString())

/** 文言内の `{...}` のうち、`{percent}` 以外のトークンを出現順・重複なしで返す。 */
fun findUnknownLmuWindowsTyreWearReadoutPlaceholders(template: String): List<String> =
    Regex("\\{[^{}]*\\}")
        .findAll(template)
        .map { it.value }
        .filter { it != LMU_WINDOWS_TYRE_WEAR_PERCENT_PLACEHOLDER }
        .distinct()
        .toList()
