package kurou.kodriver.domain.model

/** タイヤ温度警告文言の摂氏温度プレースホルダーを置換する。 */
fun formatGt7Ps5TyreTemperatureReadoutText(
    template: String,
    celsius: Int,
): String = template.replace(GT7_PS5_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER, celsius.toString())

/** 文言内の `{...}` のうち、`{celsius}` 以外のトークンを出現順・重複なしで返す。 */
fun findUnknownGt7Ps5TyreTemperatureReadoutPlaceholders(template: String): List<String> =
    Regex("\\{[^{}]*\\}")
        .findAll(template)
        .map { it.value }
        .filter { it != GT7_PS5_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER }
        .distinct()
        .toList()
