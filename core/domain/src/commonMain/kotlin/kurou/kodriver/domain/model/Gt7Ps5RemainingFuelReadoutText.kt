package kurou.kodriver.domain.model

/** 燃料残量の読み上げ文言の残量パーセントプレースホルダーを置換する。 */
fun formatGt7Ps5RemainingFuelReadoutText(
    template: String,
    percent: Int,
): String = template.replace(GT7_PS5_REMAINING_FUEL_PERCENT_PLACEHOLDER, percent.toString())

/** 文言内の `{...}` のうち、`{percent}` 以外のトークンを出現順・重複なしで返す。 */
fun findUnknownGt7Ps5RemainingFuelReadoutPlaceholders(template: String): List<String> =
    Regex("\\{[^{}]*\\}")
        .findAll(template)
        .map { it.value }
        .filter { it != GT7_PS5_REMAINING_FUEL_PERCENT_PLACEHOLDER }
        .distinct()
        .toList()
