package kurou.kodriver.domain.lmuwindows.model

import kurou.kodriver.domain.model.SinglePlaceholderTemplate

/** タイヤ温度警告文言の摂氏温度プレースホルダーを置換する。 */
public fun formatLmuWindowsTyreTemperatureReadoutText(
    template: String,
    celsius: Int,
): String =
    SinglePlaceholderTemplate(LMU_WINDOWS_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER).format(template, celsius.toString())

/** 文言内の `{...}` のうち、`{celsius}` 以外のトークンを出現順・重複なしで返す。 */
public fun findUnknownLmuWindowsTyreTemperatureReadoutPlaceholders(template: String): List<String> =
    SinglePlaceholderTemplate(LMU_WINDOWS_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER).findUnknown(template)
