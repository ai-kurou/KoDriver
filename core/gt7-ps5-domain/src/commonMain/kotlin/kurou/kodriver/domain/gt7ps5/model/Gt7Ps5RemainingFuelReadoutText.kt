package kurou.kodriver.domain.gt7ps5.model

import kurou.kodriver.domain.model.SinglePlaceholderTemplate

/** 燃料残量の読み上げ文言の残量パーセントプレースホルダーを置換する。 */
fun formatGt7Ps5RemainingFuelReadoutText(
    template: String,
    percent: Int,
): String = SinglePlaceholderTemplate(GT7_PS5_REMAINING_FUEL_PERCENT_PLACEHOLDER).format(template, percent.toString())

/** 文言内の `{...}` のうち、`{percent}` 以外のトークンを出現順・重複なしで返す。 */
fun findUnknownGt7Ps5RemainingFuelReadoutPlaceholders(template: String): List<String> =
    SinglePlaceholderTemplate(GT7_PS5_REMAINING_FUEL_PERCENT_PLACEHOLDER).findUnknown(template)
