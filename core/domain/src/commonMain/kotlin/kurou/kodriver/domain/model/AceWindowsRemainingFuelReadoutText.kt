package kurou.kodriver.domain.model

/** 燃料残量の読み上げ文言の残量パーセントプレースホルダーを置換する。 */
fun formatAceWindowsRemainingFuelReadoutText(
    template: String,
    percent: Int,
): String = template.replace(ACE_WINDOWS_REMAINING_FUEL_PERCENT_PLACEHOLDER, percent.toString())

/** 文言内の `{...}` のうち、`{percent}` 以外のトークンを出現順・重複なしで返す。 */
fun findUnknownAceWindowsRemainingFuelReadoutPlaceholders(template: String): List<String> =
    Regex("\\{[^{}]*\\}")
        .findAll(template)
        .map { it.value }
        .filter { it != ACE_WINDOWS_REMAINING_FUEL_PERCENT_PLACEHOLDER }
        .distinct()
        .toList()
