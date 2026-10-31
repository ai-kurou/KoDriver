package kurou.kodriver.domain.lmuwindows.model

import kurou.kodriver.domain.model.SinglePlaceholderTemplate

/** タイヤ残存率警告文言の閾値プレースホルダーを置換する。 */
fun formatLmuWindowsTyreWearReadoutText(
    template: String,
    percentage: Int,
): String = SinglePlaceholderTemplate(LMU_WINDOWS_TYRE_WEAR_PERCENT_PLACEHOLDER).format(template, percentage.toString())

/** 文言内の `{...}` のうち、`{percent}` 以外のトークンを出現順・重複なしで返す。 */
fun findUnknownLmuWindowsTyreWearReadoutPlaceholders(template: String): List<String> =
    SinglePlaceholderTemplate(LMU_WINDOWS_TYRE_WEAR_PERCENT_PLACEHOLDER).findUnknown(template)
