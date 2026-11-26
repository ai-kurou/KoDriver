package kurou.kodriver.domain.acewindows.model

import kurou.kodriver.domain.model.SinglePlaceholderTemplate

/** ACE のブレーキ温度警告文言の摂氏温度プレースホルダーを置換する。 */
public fun formatAceWindowsBrakeTemperatureReadoutText(
    template: String,
    celsius: Int,
): String =
    SinglePlaceholderTemplate(ACE_WINDOWS_BRAKE_TEMPERATURE_CELSIUS_PLACEHOLDER).format(template, celsius.toString())

/** 文言内の `{...}` のうち、`{celsius}` 以外のトークンを出現順・重複なしで返す。 */
public fun findUnknownAceWindowsBrakeTemperatureReadoutPlaceholders(template: String): List<String> =
    SinglePlaceholderTemplate(ACE_WINDOWS_BRAKE_TEMPERATURE_CELSIUS_PLACEHOLDER).findUnknown(template)
