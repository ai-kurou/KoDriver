package kurou.kodriver.domain.model

/** 燃料残り周回数の読み上げ文言の周回数プレースホルダーを置換する。 */
fun formatAceWindowsRemainingFuelLapsReadoutText(
    template: String,
    laps: Int,
): String = template.replace(ACE_WINDOWS_REMAINING_FUEL_LAPS_PLACEHOLDER, laps.toString())

/** 文言内の `{...}` のうち、`{laps}` 以外のトークンを出現順・重複なしで返す。 */
fun findUnknownAceWindowsRemainingFuelLapsReadoutPlaceholders(template: String): List<String> =
    Regex("\\{[^{}]*\\}")
        .findAll(template)
        .map { it.value }
        .filter { it != ACE_WINDOWS_REMAINING_FUEL_LAPS_PLACEHOLDER }
        .distinct()
        .toList()
