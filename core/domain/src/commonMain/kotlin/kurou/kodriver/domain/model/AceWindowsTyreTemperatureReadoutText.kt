package kurou.kodriver.domain.model

/** ACE のタイヤ温度警告文言の摂氏温度プレースホルダーを置換する。 */
fun formatAceWindowsTyreTemperatureReadoutText(
    template: String,
    celsius: Int,
): String = template.replace(ACE_WINDOWS_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER, celsius.toString())

/** 文言内の `{...}` のうち、`{celsius}` 以外のトークンを出現順・重複なしで返す。 */
fun findUnknownAceWindowsTyreTemperatureReadoutPlaceholders(template: String): List<String> =
    Regex("\\{[^{}]*\\}")
        .findAll(template)
        .map { it.value }
        .filter { it != ACE_WINDOWS_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER }
        .distinct()
        .toList()
