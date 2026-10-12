package kurou.kodriver.domain.model

/** タイヤ温度警告文言の摂氏温度プレースホルダーを置換する。 */
fun formatLmuWindowsTyreTemperatureReadoutText(
    template: String,
    celsius: Int,
): String = template.replace(LMU_WINDOWS_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER, celsius.toString())

/** 文言内の `{...}` のうち、`{celsius}` 以外のトークンを出現順・重複なしで返す。 */
fun findUnknownLmuWindowsTyreTemperatureReadoutPlaceholders(template: String): List<String> =
    Regex("\\{[^{}]*\\}")
        .findAll(template)
        .map { it.value }
        .filter { it != LMU_WINDOWS_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER }
        .distinct()
        .toList()
