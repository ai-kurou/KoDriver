package kurou.kodriver.domain.lmuwindows.model

import kurou.kodriver.domain.model.SinglePlaceholderTemplate

/** ブレーキ残量警告文言の閾値プレースホルダーを置換する。 */
fun formatLmuWindowsBrakeWearReadoutText(
    template: String,
    percent: Int,
): String = SinglePlaceholderTemplate(LMU_WINDOWS_BRAKE_WEAR_PERCENT_PLACEHOLDER).format(template, percent.toString())

/** 文言内の `{...}` のうち、`{percent}` 以外のトークンを出現順・重複なしで返す。 */
fun findUnknownLmuWindowsBrakeWearReadoutPlaceholders(template: String): List<String> =
    SinglePlaceholderTemplate(LMU_WINDOWS_BRAKE_WEAR_PERCENT_PLACEHOLDER).findUnknown(template)
