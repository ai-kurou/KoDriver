package kurou.kodriver.domain.acewindows.model

import kurou.kodriver.domain.model.SinglePlaceholderTemplate

/** 燃料残量の読み上げ文言の残量パーセントプレースホルダーを置換する。 */
fun formatAceWindowsRemainingFuelReadoutText(
    template: String,
    percent: Int,
): String =
    SinglePlaceholderTemplate(ACE_WINDOWS_REMAINING_FUEL_PERCENT_PLACEHOLDER).format(template, percent.toString())

/** 文言内の `{...}` のうち、`{percent}` 以外のトークンを出現順・重複なしで返す。 */
fun findUnknownAceWindowsRemainingFuelReadoutPlaceholders(template: String): List<String> =
    SinglePlaceholderTemplate(ACE_WINDOWS_REMAINING_FUEL_PERCENT_PLACEHOLDER).findUnknown(template)
