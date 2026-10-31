package kurou.kodriver.domain.lmuwindows.model

import kurou.kodriver.domain.model.SinglePlaceholderTemplate

/** ブレーキ温度警告文言の閾値プレースホルダーを置換する。 */
fun formatLmuWindowsBrakeTemperatureReadoutText(
    template: String,
    celsius: Int,
): String =
    SinglePlaceholderTemplate(LMU_WINDOWS_BRAKE_TEMPERATURE_CELSIUS_PLACEHOLDER).format(template, celsius.toString())

/** 文言内の `{...}` のうち、`{celsius}` 以外のトークンを出現順・重複なしで返す。 */
fun findUnknownLmuWindowsBrakeTemperatureReadoutPlaceholders(template: String): List<String> =
    SinglePlaceholderTemplate(LMU_WINDOWS_BRAKE_TEMPERATURE_CELSIUS_PLACEHOLDER).findUnknown(template)
