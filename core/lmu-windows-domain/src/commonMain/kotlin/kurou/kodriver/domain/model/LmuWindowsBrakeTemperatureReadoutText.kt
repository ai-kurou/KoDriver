package kurou.kodriver.domain.model

/** ブレーキ温度警告文言の閾値プレースホルダーを置換する。 */
fun formatLmuWindowsBrakeTemperatureReadoutText(
    template: String,
    celsius: Int,
): String = template.replace(LMU_WINDOWS_BRAKE_TEMPERATURE_CELSIUS_PLACEHOLDER, celsius.toString())

/** 文言内の `{...}` のうち、`{celsius}` 以外のトークンを出現順・重複なしで返す。 */
fun findUnknownLmuWindowsBrakeTemperatureReadoutPlaceholders(template: String): List<String> =
    Regex("\\{[^{}]*\\}")
        .findAll(template)
        .map { it.value }
        .filter { it != LMU_WINDOWS_BRAKE_TEMPERATURE_CELSIUS_PLACEHOLDER }
        .distinct()
        .toList()
