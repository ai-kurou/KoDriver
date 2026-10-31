package kurou.kodriver.domain.gt7ps5.model

import kurou.kodriver.domain.model.SinglePlaceholderTemplate

/** タイヤ温度警告文言の摂氏温度プレースホルダーを置換する。 */
fun formatGt7Ps5TyreTemperatureReadoutText(
    template: String,
    celsius: Int,
): String = SinglePlaceholderTemplate(GT7_PS5_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER).format(template, celsius.toString())

/** 文言内の `{...}` のうち、`{celsius}` 以外のトークンを出現順・重複なしで返す。 */
fun findUnknownGt7Ps5TyreTemperatureReadoutPlaceholders(template: String): List<String> =
    SinglePlaceholderTemplate(GT7_PS5_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER).findUnknown(template)
